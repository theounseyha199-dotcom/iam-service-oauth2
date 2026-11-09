package co.istad.iam;

import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.role.usecase.AssignPermissionsToRoleUseCase;
import kh.edu.iam.application.role.usecase.CreateRoleUseCase;
import kh.edu.iam.application.role.usecase.ListRolesUseCase;
import kh.edu.iam.application.role.usecase.RevokePermissionFromRoleUseCase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.Clock;


@SpringBootApplication(
        scanBasePackages = {"co.istad.iam", "kh.edu.iam"}
)
@EntityScan("kh.edu.iam.persistence.entity")
@EnableJpaRepositories("kh.edu.iam.persistence.repository")
public class IamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }

    @Bean
    public ListRolesUseCase listRolesUseCase(
            RoleRepository roleRepository
    ) {
        return new ListRolesUseCase(roleRepository);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public AssignPermissionsToRoleUseCase assignPermissionsToRoleUseCase(
            RolePermissionRepository repository,
            CurrentActorProvider actorProvider,
            TransactionRunner transactions,
            Clock clock
    ) {
        return new AssignPermissionsToRoleUseCase(
                repository,
                actorProvider,
                transactions,
                clock
        );
    }


    @Bean
    public RevokePermissionFromRoleUseCase revokePermissionFromRoleUseCase(
            RolePermissionRepository repository,
            CurrentActorProvider actorProvider,
            TransactionRunner transactions,
            Clock clock
    ) {
        return new RevokePermissionFromRoleUseCase(
                repository,
                actorProvider,
                transactions,
                clock
        );
    }

    @Bean
    public CreateRoleUseCase createRoleUseCase(
            RoleRepository roleRepository,
            AssignPermissionsToRoleUseCase assignPermissions,
            CurrentActorProvider actorProvider,
            TransactionRunner transactions,
            Clock clock
    ) {
        return new CreateRoleUseCase(
                roleRepository,
                assignPermissions,
                actorProvider,
                transactions,
                clock
        );
    }
}
