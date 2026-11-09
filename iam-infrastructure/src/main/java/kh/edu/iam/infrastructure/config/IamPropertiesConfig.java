package kh.edu.iam.infrastructure.config;

import kh.edu.iam.infrastructure.properties.ClientSeedProperties;
import kh.edu.iam.infrastructure.properties.TokenAudienceProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ClientSeedProperties.class, TokenAudienceProperties.class})
public class IamPropertiesConfig {}
