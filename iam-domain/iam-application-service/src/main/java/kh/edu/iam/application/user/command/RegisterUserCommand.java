package kh.edu.iam.application.user.command;

public record RegisterUserCommand(
        String username,
        String email,
        String password,
        String confirmPassword,
        String givenName,
        String familyName,
        String locale) {

    @Override
    public String toString() {
        return "RegisterUserCommand[passwords=REDACTED]";
    }
}
