package kh.edu.iam.restapi.controller;

import jakarta.validation.Valid;

import kh.edu.iam.application.user.usecase.RegisterUserUseCase;
import kh.edu.iam.restapi.dto.Requests;
import kh.edu.iam.restapi.dto.Responses;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name="Registration", description="Create an account without an access token.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegisterUserUseCase registerUserUseCase;

    @io.swagger.v3.oas.annotations.Operation(summary="Register account")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.User register(@Valid @RequestBody Requests.Register request) {
        return Responses.user(registerUserUseCase.execute(request.command()));
    }
}
