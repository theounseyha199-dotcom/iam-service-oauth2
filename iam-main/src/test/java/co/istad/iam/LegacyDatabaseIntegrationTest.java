package co.istad.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

class LegacyDatabaseIntegrationTest extends IamIntegrationTestSupport {
    @Test
    void legacySchemaAdoptionPreservesCoreDataAndCreatesWorkingAdministrator() {
        String schema = "legacy_dashboard_test";
        jdbc.execute("CREATE SCHEMA " + schema);
        for (String table : List.of("users", "roles", "permissions", "identities", "user_roles",
            "user_permissions", "role_permissions", "oauth2_registered_client", "oauth2_authorization",
            "oauth2_authorization_consent", "databasechangelog")) {
            jdbc.execute("CREATE TABLE " + schema + "." + table + " (LIKE public." + table + " INCLUDING ALL)");
        }
        jdbc.execute("INSERT INTO " + schema + ".roles SELECT * FROM public.roles");
        jdbc.execute(
            "INSERT INTO " + schema + ".permissions SELECT * FROM public.permissions WHERE resource IN ('user','role','permission')");
        jdbc.execute("INSERT INTO " + schema + ".role_permissions SELECT rp.* FROM public.role_permissions rp JOIN "
            + schema + ".permissions p ON p.id=rp.permission_id");
        jdbc.execute(
            "INSERT INTO " + schema + ".oauth2_registered_client SELECT * FROM public.oauth2_registered_client");
        jdbc.execute(
            "INSERT INTO " + schema + ".databasechangelog SELECT * FROM public.databasechangelog WHERE id='001-create-permissions'");
        for (String table : List.of("user_roles", "user_permissions", "role_permissions")) {
            jdbc.execute("ALTER TABLE " + schema + "." + table + " DROP COLUMN revocation_reason");
        }
        // LIKE copies identity definitions, but copied seed IDs still need their next values advanced.
        for (String table : List.of("roles", "permissions", "role_permissions")) {
            jdbc.execute("SELECT setval(pg_get_serial_sequence('" + schema + "." + table + "','id'),"
                + "COALESCE((SELECT max(id) FROM " + schema + "." + table + "),1),true)");
        }
        String url = context.getEnvironment().getProperty("spring.datasource.url");
        url += (url.contains("?") ? "&" : "?") + "currentSchema=" + schema;
        try (var legacy = new SpringApplicationBuilder(IamServiceApplication.class).run(
            "--server.port=0",
            "--spring.main.banner-mode=off",
            "--spring.datasource.url=" + url,
            "--spring.datasource.username=" + context.getEnvironment().getProperty("spring.datasource.username"),
            "--spring.datasource.password=" + context.getEnvironment().getProperty("spring.datasource.password", ""),
            "--spring.jpa.properties.hibernate.default_schema=" + schema,
            "--spring.liquibase.change-log=classpath:db/changelog/changes/changelog-legacy.yaml",
            "--spring.mail.host=127.0.0.1", "--spring.mail.port=1", "--spring.mail.test-connection=false",
            "--spring.data.redis.port=" + context.getEnvironment().getProperty("spring.data.redis.port"),
            "--app.iam.clients.oidc.secret=test-client-secret", "--IAM_DCR_CLIENT_SECRET=test-dcr-secret",
            "--IAM_ADMIN_USERNAME=legacy-admin", "--IAM_ADMIN_EMAIL=legacy-admin@example.com",
            "--IAM_ADMIN_PASSWORD=" + PASSWORD,
            "--app.iam.signing-key-file=" + context.getEnvironment().getProperty("app.iam.signing-key-file"))) {
            var database = legacy.getBean(JdbcTemplate.class);
            assertEquals(2, database.queryForObject("SELECT count(*) FROM roles", Integer.class));
            assertEquals(1,
                database.queryForObject("SELECT count(*) FROM databasechangelog WHERE id='001-create-permissions'",
                    Integer.class));
            assertEquals(1,
                database.queryForObject("SELECT count(*) FROM users WHERE username='legacy-admin'", Integer.class));
            assertEquals(3, database.queryForObject(
                "SELECT count(*) FROM information_schema.columns WHERE table_schema=? AND column_name='revocation_reason'",
                Integer.class, schema));
            var user = legacy.getBean(kh.edu.iam.application.port.out.UserRepository.class)
                .findByUsernameOrEmail("legacy-admin").orElseThrow();
            var permissions = legacy.getBean(kh.edu.iam.application.user.usecase.GetUserAuthoritiesUseCase.class)
                .execute(user.getUuid()).effectivePermissions();
            assertTrue(permissions.containsAll(List.of("user:read", "client:create", "audit:read", "policy:update")));
        }
    }
}
