package kh.edu.iam.restapi.controller;

import jakarta.validation.Valid;

import kh.edu.iam.application.identity.usecase.ListUserIdentitiesUseCase;
import kh.edu.iam.application.identity.usecase.UnlinkIdentityUseCase;
import kh.edu.iam.application.user.usecase.ChangePasswordUseCase;
import kh.edu.iam.application.user.usecase.GetUserUseCase;
import kh.edu.iam.application.user.usecase.UpdateUserProfileUseCase;
import kh.edu.iam.restapi.dto.ProfileRequest;
import kh.edu.iam.restapi.dto.Requests;
import kh.edu.iam.restapi.dto.Responses;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final ListUserIdentitiesUseCase listUserIdentitiesUseCase;
    private final UnlinkIdentityUseCase unlinkIdentityUseCase;

    private UUID subject(org.springframework.security.oauth2.jwt.Jwt jwt) {
        try {
            var uuid = UUID.fromString(jwt.getSubject());
            if (!uuid.toString().equalsIgnoreCase(jwt.getSubject())) {
                throw new IllegalArgumentException();
            }
            return uuid;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new org.springframework.security.access.AccessDeniedException("An internal user subject is required");
        }
    }

    @GetMapping
    public Responses.User get(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
                    org.springframework.security.oauth2.jwt.Jwt jwt) {
        return Responses.user(getUserUseCase.execute(subject(jwt)));
    }

    @PatchMapping
    public Responses.User update(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
                    org.springframework.security.oauth2.jwt.Jwt jwt,
            @Valid @RequestBody ProfileRequest request) {
        return Responses.user(updateUserProfileUseCase.execute(request.command(subject(jwt))));
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
                    org.springframework.security.oauth2.jwt.Jwt jwt,
            @Valid @RequestBody Requests.ChangePassword request) {
        changePasswordUseCase.execute(request.command(subject(jwt)));
    }

    @GetMapping("/identities")
    public List<Responses.Identity> identities(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
                    org.springframework.security.oauth2.jwt.Jwt jwt) {
        return listUserIdentitiesUseCase.execute(subject(jwt)).stream().map(Responses::identity).toList();
    }

    @DeleteMapping("/identities/{identityUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlink(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
                    org.springframework.security.oauth2.jwt.Jwt jwt,
            @PathVariable("identityUuid") UUID identityUuid) {
        unlinkIdentityUseCase.execute(subject(jwt), identityUuid);
    }
}
