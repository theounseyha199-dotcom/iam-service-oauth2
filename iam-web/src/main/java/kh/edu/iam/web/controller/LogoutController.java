package kh.edu.iam.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** CSRF-protected logout confirmation for clients without an OIDC ID-token hint. */
@Controller
public class LogoutController {
    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }
}
