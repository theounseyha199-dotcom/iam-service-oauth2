import { Link } from "@/components/account/AccountLink";

export function BackToLogin() {
    return (
        <p>
            <Link to="/login" label="recovery.back" />
        </p>
    );
}
