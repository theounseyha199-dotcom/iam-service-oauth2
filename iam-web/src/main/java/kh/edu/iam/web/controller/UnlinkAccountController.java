package kh.edu.iam.web.controller;

import kh.edu.iam.application.identity.usecase.ListUserIdentitiesUseCase;
import kh.edu.iam.application.identity.usecase.UnlinkIdentityUseCase;
import kh.edu.iam.web.view.BrowserUser;
import kh.edu.iam.web.view.Forms;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UnlinkAccountController {
    private final BrowserUser current;
    private final ListUserIdentitiesUseCase list;
    private final UnlinkIdentityUseCase unlink;

    @GetMapping("/profile/accounts/{id}/unlink")
    public String confirm(@PathVariable("id") UUID id, Model model) {
        var identity =
                list.execute(current.current().uuid()).stream()
                        .filter(i -> i.uuid().equals(id))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new org.springframework.web.server.ResponseStatusException(
                                                org.springframework.http.HttpStatus.NOT_FOUND));
        model.addAttribute("identity", identity);
        return "unlink-account";
    }

    @PostMapping("/profile/accounts/{id}/unlink")
    public String unlink(@PathVariable("id") UUID id, RedirectAttributes redirect) {
        var user = current.current();
        try {
            unlink.execute(user.uuid(), id);
            return "redirect:/profile/accounts?unlinked";
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("errorCode", Forms.code(ex));
            return "redirect:/profile/accounts?error";
        }
    }
}
