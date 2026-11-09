# Sources and adaptation notes

The authoritative requirements are the user-supplied **III. Assignment - Spring
Authorization Server & IAM Service.pdf**, Sections A–F and the best-practices block.

The user authorized reuse from `/home/seyha/web/spring-microservice/istad-ite-1`.
The sibling `microservices/iam-service` supplied REST/security/JTE adapter patterns,
message bundles, shared browser controls, JDBC serialization and Postman resources.
They were adapted to this project's existing mutable domain, package structure,
repository ports, commands, results, transactions and Liquibase history. No sibling
source files, databases, signing keys or provider registrations were modified.
Provider credentials are reused only through ignored `.env.local` (mode 0600);
tracked configuration contains environment placeholders.

Official technical sources used to verify the adapters:

- [Spring Security Authorization Server configuration](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/authorization-server/configuration-model.html)
- [Spring Security protocol endpoints](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/authorization-server/protocol-endpoints.html)
- [Spring Security OAuth client interfaces](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/client/core.html)
- [JTE precompiled templates](https://jte.gg/pre-compiling/)
- [JTE Spring Boot 4 starter](https://jte.gg/spring-boot-starter-4/)
- [Telegram OIDC protocol and supported scopes](https://core.telegram.org/bots/telegram-login)

The project compiles with its existing Spring Boot 4.1.1 / Spring Security 7.1.1
and Java 25 dependencies. Cached dependency sources were also checked for the JDBC
JSON mapper and DCR's PKCE/consent defaults. Telegram uses `openid profile`, a signed
ID token, the Telegram issuer and JWKS; it has no separate UserInfo endpoint and
no email scope. Missing required fields enter a short-lived completion flow.

Live provider account consent/MFA is deliberately not automated. Each provider
must approve the application's configured redirect URI. The reference's external
tunnel URLs were not silently adopted as this project's public address.
