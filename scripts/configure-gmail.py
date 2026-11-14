#!/usr/bin/env python3
"""Configure Gmail privately only after authentication and a real test send succeed."""
from email.message import EmailMessage
from getpass import getpass
from pathlib import Path
import os
import re
import shlex
import smtplib
import ssl
import tempfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def private_write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    descriptor, temporary = tempfile.mkstemp(prefix=".gmail-", dir=path.parent)
    try:
        with os.fdopen(descriptor, "w") as file:
            file.write(text)
        os.replace(temporary, path)
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)


def save_settings(username, password):
    values = {
        "SPRING_PROFILES_ACTIVE": "gmail",
        "GMAIL_USERNAME": username,
        "GMAIL_APP_PASSWORD": password,
    }
    env_file = ROOT / ".env.local"
    existing = env_file.read_text() if env_file.exists() else ""
    lines = [line for line in existing.splitlines()
             if line.split("=", 1)[0].strip().removeprefix("export ") not in values]
    lines.extend(f"{key}={shlex.quote(value)}" for key, value in values.items())
    run_file = ROOT / ".run/IAM Local.private.run.xml"
    tree = ET.parse(run_file) if run_file.exists() else None
    if tree is not None:
        configuration = tree.find(".//configuration")
        if configuration is None:
            raise ValueError("The private IntelliJ configuration is invalid")
        envs = configuration.find("envs")
        if envs is None:
            envs = ET.SubElement(configuration, "envs")
        for key, value in values.items():
            entry = next((entry for entry in envs if entry.get("name") == key), None)
            if entry is None:
                ET.SubElement(envs, "env", {"name": key, "value": value})
            else:
                entry.set("value", value)
        ET.indent(tree, space="  ")
    private_write(env_file, "\n".join(lines) + "\n")
    if tree is not None:
        private_write(run_file, ET.tostring(tree.getroot(), encoding="unicode") + "\n")


def main():
    print("Create a Google app password at https://myaccount.google.com/apppasswords")
    print("Your Google account must have 2-Step Verification enabled.")
    print("The password is entered privately; it is never printed or added to Git.")
    username = input("Gmail sender address: ").strip().lower()
    if not re.fullmatch(r"[^\s@<>]+@[^\s@<>]+\.[^\s@<>]+", username):
        raise ValueError("Enter a valid Gmail/Google Workspace address")
    password = getpass("Google app password (hidden): ").replace(" ", "")
    if not re.fullmatch(r"[A-Za-z0-9]{16}", password):
        raise ValueError("Use Google's 16-character app password, not your Google account password")
    recipient = input(f"Send the delivery test to [{username}]: ").strip() or username
    if not re.fullmatch(r"[^\s@<>]+@[^\s@<>]+\.[^\s@<>]+", recipient):
        raise ValueError("Enter a valid test recipient")
    message = EmailMessage()
    message["From"] = username
    message["To"] = recipient
    message["Subject"] = "ISTAD Identity Gmail delivery test"
    message.set_content(
        "Gmail delivery is connected to ISTAD Identity.\n\n"
        "Register at http://localhost:9000/register to receive your verification link, "
        "or recover your password at http://localhost:9000/forgot-password.\n\n"
        "This delivery test does not verify an account or change a password."
    )
    print("Testing Gmail authentication and sending your test email...")
    with smtplib.SMTP("smtp.gmail.com", 587, timeout=15) as smtp:
        smtp.ehlo()
        smtp.starttls(context=ssl.create_default_context())
        smtp.ehlo()
        smtp.login(username, password)
        if smtp.send_message(message):
            raise ValueError("Gmail did not accept the test recipient; configuration was not changed")
    save_settings(username, password)
    print("Gmail accepted the test email. Check your inbox and spam folder.")
    print("Saved private settings to .env.local and the existing IntelliJ run configuration.")
    print("Stop IAM if it is running, then restart it from IntelliJ or run: bash scripts/run-local.sh")
    print("The gmail profile also checks SMTP connectivity on every IAM startup.")


if __name__ == "__main__":
    try:
        main()
    except (EOFError, KeyboardInterrupt):
        print("\nCancelled; Gmail setup did not complete.")
        raise SystemExit(1)
    except smtplib.SMTPAuthenticationError:
        print("Gmail rejected authentication. Check 2-Step Verification and create a new app password.")
        raise SystemExit(1)
    except (smtplib.SMTPException, OSError) as error:
        print(f"Gmail setup failed ({type(error).__name__}). Check your connection and try again.")
        raise SystemExit(1)
    except (ValueError, ET.ParseError) as error:
        print(str(error))
        raise SystemExit(1)
