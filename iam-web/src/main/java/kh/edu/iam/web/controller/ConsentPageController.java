package kh.edu.iam.web.controller;

import kh.edu.iam.application.account.usecase.GetConsentClientUseCase;
import kh.edu.iam.web.view.BrowserUser;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.TreeSet;

@Controller
@RequiredArgsConstructor
public class ConsentPageController {
    private final GetConsentClientUseCase clients;
    private final BrowserUser current;

    @GetMapping("/oauth2/consent")
    public String consent(
            @RequestParam("client_id") String clientId,
            @RequestParam("scope") String scope,
            @RequestParam("state") String state,
            Model model) {
        current.current();
        if (state.isBlank() || state.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            var scopes = new TreeSet<>(Arrays.asList(scope.split(" +")));
            var client = clients.execute(clientId, scopes);
            model.addAttribute("client", client);
            model.addAttribute("clientId", clientId);
            model.addAttribute("state", state);
            return "consent";
        } catch (kh.edu.iam.application.exception.ApplicationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
