package co.istad.iam;

import kh.edu.iam.User;
import kh.edu.iam.UserProfile;
import kh.edu.iam.UserRole;
import kh.edu.iam.application.port.out.PasswordHasher;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.port.out.UserRepository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Clock;

@Configuration
public class BootstrapAdministrator {
    @Bean
    public ApplicationRunner bootstrapAdministratorRunner(
            Environment env,
            UserRepository users,
            RoleRepository roles,
            UserAssignmentRepository assignments,
            PasswordHasher passwords,
            TransactionRunner transactions,
            Clock clock) {
        return args -> {
            String password = env.getProperty("IAM_ADMIN_PASSWORD");
            if (password == null || password.isBlank()) {
                return;
            }
            transactions.required(
                    () -> {
                        String username = env.getProperty("IAM_ADMIN_USERNAME", "admin");
                        String email = env.getProperty("IAM_ADMIN_EMAIL", "admin@istad.local");
                        if (users.existsByUsername(username) || users.existsByEmail(email)) {
                            return null;
                        }
                        var now = clock.instant();
                        var user =
                                users.save(
                                        User.create(
                                                username,
                                                email,
                                                passwords.hash(password),
                                                UserProfile.basic("IAM", "Administrator", "en"),
                                                "bootstrap",
                                                now));
                        var role = roles.findByName("ADMIN").orElseThrow();
                        assignments.insertRole(
                                UserRole.assign(
                                        user.getId(), role.getId(), "bootstrap", now, null, "Initial administrator"));
                        return null;
                    });
        };
    }
}
