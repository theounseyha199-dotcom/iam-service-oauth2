package kh.edu.iam.infrastructure.config;

import kh.edu.iam.application.user.result.AuthUserResult;
import kh.edu.iam.infrastructure.security.IamUserPrincipal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
public class PrincipalConfig {
    @Bean
    public Function<AuthUserResult, UserDetails> federatedUserDetailsFactory() {
        return IamUserPrincipal::fromFederated;
    }
}
