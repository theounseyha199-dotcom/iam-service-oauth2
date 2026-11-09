package co.istad.iam;

import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.role.usecase.ListRolesUseCase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


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
}
