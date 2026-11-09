package kh.edu.iam.restapi.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import kh.edu.iam.Address;
import kh.edu.iam.PermissionEffect;
import kh.edu.iam.UserProfile;
import kh.edu.iam.application.permission.command.CreatePermissionCommand;
import kh.edu.iam.application.role.command.CreateRoleCommand;
import kh.edu.iam.application.user.command.ChangePasswordCommand;
import kh.edu.iam.application.user.command.CreateUserCommand;
import kh.edu.iam.application.user.command.RegisterUserCommand;
import kh.edu.iam.application.user.command.ResetUserPasswordCommand;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * HTTP inputs never bind directly to domain entities or authentication results.
 */
public final class Requests {
    private Requests() {}

    public interface Strict {
        @JsonAnySetter
        default void unknown(String name, Object value) {
            throw new IllegalArgumentException("Unsupported request field");
        }
    }

    public record Register(
            @NotBlank @Size(max = 50) String username,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank String confirmPassword,
            @NotBlank @Size(max = 100) String givenName,
            @NotBlank @Size(max = 100) String familyName,
            @Pattern(regexp = "en|km") String locale)
            implements Strict {
        public RegisterUserCommand command() {
            return new RegisterUserCommand(username, email, password, confirmPassword, givenName, familyName, locale);
        }

        @Override
        public String toString() {
            return "Register[redacted]";
        }
    }

    public record AddressInput(
            @Size(max = 255) String streetAddress,
            @Size(max = 100) String locality,
            @Size(max = 100) String region,
            @Size(max = 20) String postalCode,
            @Pattern(regexp = "[a-zA-Z]{2}") String country)
            implements Strict {
        public Address value() {
            return new Address(streetAddress, locality, region, postalCode, country);
        }
    }

    public record CreateUser(
            @NotBlank @Size(max = 50) String username,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 100) String givenName,
            @NotBlank @Size(max = 100) String familyName,
            @Size(max = 100) String middleName,
            @Size(max = 100) String nickname,
            @Pattern(regexp = "male|female|other") String gender,
            LocalDate birthdate,
            @Size(max = 500) String profileUrl,
            @Size(max = 500) String pictureUrl,
            @Size(max = 500) String websiteUrl,
            @Size(max = 50) String zoneinfo,
            @Pattern(regexp = "en|km") String locale,
            @Pattern(regexp = "\\+[1-9][0-9]{1,14}") String phoneNumber,
            @Valid AddressInput address,
            @Size(max = 100) List<@NotNull UUID> roleUuids)
            implements Strict {
        public CreateUserCommand command() {
            return new CreateUserCommand(
                    username,
                    email,
                    password,
                    new UserProfile(
                            givenName,
                            familyName,
                            middleName,
                            nickname,
                            gender,
                            birthdate,
                            profileUrl,
                            pictureUrl,
                            websiteUrl,
                            zoneinfo,
                            locale == null ? "en" : locale,
                            phoneNumber,
                            false,
                            address == null ? null : address.value()),
                    roleUuids);
        }

        @Override
        public String toString() {
            return "CreateUser[redacted]";
        }
    }

    public record ChangePassword(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 72) String newPassword,
            @NotBlank String confirmPassword)
            implements Strict {
        public ChangePasswordCommand command(UUID uuid) {
            return new ChangePasswordCommand(uuid, currentPassword, newPassword, confirmPassword);
        }

        @Override
        public String toString() {
            return "ChangePassword[redacted]";
        }
    }

    public record ResetPassword(@NotBlank @Size(min = 8, max = 72) String newPassword, @NotBlank String confirmPassword)
            implements Strict {
        public ResetUserPasswordCommand command(UUID uuid) {
            return new ResetUserPasswordCommand(uuid, newPassword, confirmPassword);
        }

        @Override
        public String toString() {
            return "ResetPassword[redacted]";
        }
    }

    public record AssignRoles(
            @NotEmpty @Size(max = 100) List<@NotNull UUID> roleUuids,
            @Future Instant expiresAt,
            @Size(max = 255) String reason)
            implements Strict {}

    public record AssignPermissions(
            @NotEmpty @Size(max = 100) List<@NotNull UUID> permissionUuids,
            @Future Instant expiresAt,
            @Size(max = 255) String reason)
            implements Strict {}

    public record GrantPermissions(
            @NotEmpty @Size(max = 100) List<@NotNull UUID> permissionUuids,
            @NotNull PermissionEffect effect,
            @Future Instant expiresAt,
            @Size(max = 255) String reason)
            implements Strict {}

    public record CreateRole(
            @NotBlank @Size(max = 50) String name,
            @Size(max = 255) String description,
            @Size(max = 100) List<@NotNull UUID> permissionUuids)
            implements Strict {
        public CreateRoleCommand command() {
            return new CreateRoleCommand(name, description, permissionUuids);
        }
    }

    public record CreatePermission(
            @NotBlank @Size(max = 50) String resource,
            @NotBlank @Size(max = 50) String action,
            @Size(max = 255) String description)
            implements Strict {
        public CreatePermissionCommand command() {
            return new CreatePermissionCommand(resource, action, description);
        }
    }

    public record Revoke(@NotBlank @Size(max = 255) String reason) implements Strict {}
}
