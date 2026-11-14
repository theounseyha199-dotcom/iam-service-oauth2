package kh.edu.iam.web.controller;

import kh.edu.iam.application.port.out.EmailVerification;
import kh.edu.iam.application.exception.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;

@Controller
public class EmailVerificationController {
    private final EmailVerification verification;
    public EmailVerificationController(EmailVerification verification) { this.verification = verification; }

    @GetMapping("/registration-pending")
    public String pending() { return "redirect:/verify-email"; }

    @GetMapping("/resend-verification")
    public String resendForm(HttpServletRequest request, Model model, HttpServletResponse response) {
        initialize(request, response, model);
        model.addAttribute("sent", false);
        return "registration-pending";
    }

    @PostMapping("/resend-verification")
    public String resend(@RequestParam(name="email", defaultValue="") String email,
            HttpServletRequest request, HttpServletResponse response, Model model) {
        initialize(request, response, model);
        email = email.strip().toLowerCase(Locale.ROOT);
        model.addAttribute("email", email);
        model.addAttribute("sent", false);
        try { kh.edu.iam.domain.rule.Checks.email(email); }
        catch (RuntimeException ex) {
            response.setStatus(400); model.addAttribute("errorCode", "validation"); return "registration-pending";
        }
        var session = request.getSession();
        boolean allowed;
        synchronized (session) {
            Long previous = (Long) session.getAttribute("verificationRequestedAt");
            allowed = previous == null || System.currentTimeMillis() - previous >= 60000;
            if (allowed) session.setAttribute("verificationRequestedAt", System.currentTimeMillis());
            session.setAttribute("verificationEmail", email);
        }
        if (allowed) verification.request(email);
        // Identical feedback whether an account exists, is verified, or is throttled.
        model.addAttribute("sent", true);
        return "registration-pending";
    }

    @GetMapping("/verify-email")
    public String form(HttpServletRequest request, HttpServletResponse response, Model model) {
        initialize(request, response, model);
        return "verify-email";
    }

    @PostMapping("/verify-email")
    public String verify(@RequestParam(name="email", defaultValue="") String email,
            @RequestParam(name="code", defaultValue="") String code,
            HttpServletRequest request, HttpServletResponse response, Model model) {
        initialize(request, response, model);
        email = email.strip().toLowerCase(Locale.ROOT);
        model.addAttribute("email", email);
        try {
            verification.verify(email, code.strip());
            request.getSession().removeAttribute("verificationEmail");
            model.addAttribute("done", true);
        } catch (ApplicationException ex) {
            response.setStatus(400); model.addAttribute("errorCode", ex.code());
        } catch (RuntimeException ex) {
            response.setStatus(503); model.addAttribute("errorCode", "generic");
        }
        // Never echo the submitted code into the React bootstrap.
        return "verify-email";
    }

    private static void initialize(HttpServletRequest request, HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        model.addAttribute("errorCode", "");
        model.addAttribute("done", false);
        var session = request.getSession(false);
        Object email = session == null ? null : session.getAttribute("verificationEmail");
        model.addAttribute("email", email instanceof String value ? value : "");
    }
}
