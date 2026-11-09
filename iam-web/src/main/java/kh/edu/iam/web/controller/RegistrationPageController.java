package kh.edu.iam.web.controller;

import jakarta.servlet.http.HttpServletResponse;

import kh.edu.iam.application.user.command.RegisterUserCommand;
import kh.edu.iam.application.user.usecase.RegisterUserUseCase;
import kh.edu.iam.domain.rule.Checks;
import kh.edu.iam.web.view.Forms;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RegistrationPageController {
    private final RegisterUserUseCase register;

    @GetMapping("/register")
    public String form(Model model) {
        model.addAttribute("values", Map.of());
        model.addAttribute("errors", Map.of());
        model.addAttribute("errorCode", "");
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam Map<String, String> values, Model model, HttpServletResponse response) {
        var errors = new LinkedHashMap<String, String>();
        for (String key : List.of("username", "email", "givenName", "familyName", "password", "confirmPassword")) {
            if (values.getOrDefault(key, "").isBlank()) {
                errors.put(key, "validation.required");
            }
        }
        if (!values.getOrDefault("username", "").matches("[A-Za-z0-9._-]{3,50}")) {
            errors.put("username", "validation.username");
        }
        try {
            Checks.email(values.get("email"));
        } catch (RuntimeException ex) {
            errors.put("email", "validation.email");
        }
        for (String key : List.of("givenName", "familyName")) {
            if (values.getOrDefault(key, "").length() > 100) {
                errors.put(key, "validation.name");
            }
        }
        try {
            kh.edu.iam.application.common.PasswordPolicy.validate(
                    values.get("password"), values.get("confirmPassword"));
        } catch (IllegalArgumentException | NullPointerException ex) {
            boolean mismatch = !java.util.Objects.equals(values.get("password"), values.get("confirmPassword"));
            errors.put(
                    mismatch ? "confirmPassword" : "password", mismatch ? "validation.confirm" : "validation.password");
        }
        String error = "validation";
        if (errors.isEmpty()) {
            try {
                register.execute(
                        new RegisterUserCommand(
                                values.get("username"),
                                values.get("email"),
                                values.get("password"),
                                values.get("confirmPassword"),
                                values.get("givenName"),
                                values.get("familyName"),
                                values.getOrDefault("locale", "en")));
                return "redirect:/login?registered";
            } catch (RuntimeException ex) {
                error = Forms.code(ex);
            }
        }
        response.setStatus(400);
        model.addAttribute("values", Forms.values(values));
        model.addAttribute("errors", errors);
        model.addAttribute("errorCode", error);
        return "register";
    }
}
