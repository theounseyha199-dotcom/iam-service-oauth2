package co.istad.iam.config;

import kh.edu.iam.application.identity.service.IdentityApplicationSupport;
import kh.edu.iam.application.port.out.IdentityRepository;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public IdentityApplicationSupport identityApplicationSupport(IdentityRepository identities, Clock clock) {
        return new IdentityApplicationSupport(identities, clock);
    }

    @Bean
    public UserApplicationSupport userApplicationSupport(
            UserRepository users,
            RoleRepository roles,
            PermissionRepository permissions,
            UserAssignmentRepository assignments,
            RolePermissionRepository roleAssignments,
            Clock clock) {
        return new UserApplicationSupport(users, roles, permissions, assignments, roleAssignments, clock);
    }
}
