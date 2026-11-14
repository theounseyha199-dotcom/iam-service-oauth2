package kh.edu.iam.web.controller;

import kh.edu.iam.application.port.out.PasswordRecovery;
import kh.edu.iam.application.exception.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;

@Controller
public class PasswordRecoveryController {
    private final PasswordRecovery recovery;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordRecoveryController.class);
    public PasswordRecoveryController(PasswordRecovery recovery) { this.recovery = recovery; }

    @GetMapping("/forgot-password")
    public String forgot(Model model, HttpServletResponse response) { protect(response); model.addAttribute("sent", false); model.addAttribute("errorCode", ""); model.addAttribute("email", ""); return "forgot-password"; }

    @PostMapping("/forgot-password")
    public String request(@RequestParam(name="email", defaultValue="") String email, HttpServletRequest request,
            HttpServletResponse response, Model model) {
        protect(response);
        email = email.strip().toLowerCase(Locale.ROOT);
        model.addAttribute("email", email); model.addAttribute("sent", false); model.addAttribute("errorCode", "");
        try { kh.edu.iam.domain.rule.Checks.email(email); }
        catch (RuntimeException ex) { response.setStatus(400); model.addAttribute("errorCode", "validation"); return "forgot-password"; }
        var session = request.getSession();
        synchronized (session) {
            Long last = (Long) session.getAttribute("recoveryRequestedAt");
            if (last != null && System.currentTimeMillis() - last < 60000) {
                model.addAttribute("sent", true); return "forgot-password";
            }
            session.setAttribute("recoveryRequestedAt", System.currentTimeMillis());
        }
        try { recovery.request(email); }
        catch (RuntimeException ex) {
            // No recipient, link, exception message or token in logs or browser responses.
            log.warn("Password recovery delivery failed ({})", ex.getClass().getSimpleName());
        }
        model.addAttribute("sent", true);
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String reset(@RequestParam(name="token", defaultValue="") String token, HttpServletRequest request,
            HttpServletResponse response, Model model) {
        protect(response);
        if (!token.isEmpty()) {
            request.getSession().setAttribute("passwordResetToken", token.matches("[A-Za-z0-9_-]{43}") ? token : "");
            return "redirect:/reset-password";
        }
        model.addAttribute("errorCode", request.getSession().getAttribute("passwordResetToken") == null ? "invalid_reset_token" : "");
        model.addAttribute("done", false);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(@RequestParam(name="newPassword", defaultValue="") String newPassword,
            @RequestParam(name="confirmPassword", defaultValue="") String confirmPassword, HttpServletRequest request,
            HttpServletResponse response, Model model) {
        protect(response); model.addAttribute("done", false); model.addAttribute("errorCode", "");
        try {
            recovery.reset((String) request.getSession().getAttribute("passwordResetToken"), newPassword, confirmPassword);
            request.getSession().removeAttribute("passwordResetToken");
            model.addAttribute("done", true);
        } catch (IllegalArgumentException ex) {
            response.setStatus(400); model.addAttribute("errorCode", "validation");
        } catch (ApplicationException ex) {
            response.setStatus(400); model.addAttribute("errorCode", ex.code());
        }
        return "reset-password";
    }

    private static void protect(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store"); response.setHeader("Referrer-Policy", "no-referrer");
    }
}
