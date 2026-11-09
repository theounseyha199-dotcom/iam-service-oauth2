package co.istad.iam;

import static org.junit.jupiter.api.Assertions.*;

import kh.edu.iam.IdentityProvider;
import kh.edu.iam.application.identity.command.FederatedIdentityCommand;
import kh.edu.iam.application.permission.command.CreatePermissionCommand;
import kh.edu.iam.application.permission.usecase.CreatePermissionUseCase;
import kh.edu.iam.application.role.command.CreateRoleCommand;
import kh.edu.iam.application.role.result.RoleResult;
import kh.edu.iam.application.role.usecase.CreateRoleUseCase;
import kh.edu.iam.application.user.command.RegisterUserCommand;
import kh.edu.iam.application.user.result.UserResult;
import kh.edu.iam.application.user.usecase.RegisterUserUseCase;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Never runs against the normal application database.
 */
@EnabledIf("databaseTestsEnabled")
abstract class IamIntegrationTestSupport {
    static ConfigurableApplicationContext context;
    static JdbcTemplate jdbc;

    static <T> T bean(Class<T> type) {
        return context.getBean(type);
    }

    protected static String unique() {
        return "u" + UUID.randomUUID().toString().replace("-", "");
    }

    protected static final String PASSWORD = "Str0ng!Pass2026";

    private static Path cluster;

    protected static boolean databaseTestsEnabled() {
        return Boolean.getBoolean("iam.part2.integration") || System.getenv("IAM_TEST_DB_URL") != null;
    }

    private static void command(String... args) throws Exception {
        var log = cluster.resolve("process.log");
        var process = new ProcessBuilder(args).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("PostgreSQL command timed out");
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException(Files.readString(log));
        }
    }

    @BeforeAll
    static void start() throws Exception {
        String url = System.getenv("IAM_TEST_DB_URL");
        String username = System.getenv().getOrDefault("IAM_TEST_DB_USERNAME", System.getProperty("user.name"));
        String password = System.getenv().getOrDefault("IAM_TEST_DB_PASSWORD", "");
        if (url == null) {
            cluster = Files.createTempDirectory("iam-part2-postgres-");
            int port;
            try (var socket = new ServerSocket(0)) {
                port = socket.getLocalPort();
            }
            command("initdb", "-D", cluster.resolve("data").toString(), "-A", "trust", "-U", username);
            command(
                    "pg_ctl",
                    "-D",
                    cluster.resolve("data").toString(),
                    "-l",
                    cluster.resolve("server.log").toString(),
                    "-o",
                    "-h 127.0.0.1 -p " + port + " -k " + cluster,
                    "-w",
                    "start");
            command("createdb", "-h", "127.0.0.1", "-p", String.valueOf(port), "-U", username, "iam_part2_test");
            url = "jdbc:postgresql://127.0.0.1:" + port + "/iam_part2_test";
        }
        if (!url.matches(".*/iam_part2_test(?:\\?.*)?")) {
            throw new IllegalArgumentException("Tests require a disposable database named iam_part2_test");
        }
        context =
                new SpringApplicationBuilder(IamServiceApplication.class)
                        .run(
                                "--spring.main.web-application-type=servlet",
                                "--server.port=0",
                                "--GOOGLE_CLIENT_ID=test-google",
                                "--GOOGLE_CLIENT_SECRET=test-google-secret",
                                "--GITHUB_CLIENT_ID=test-github",
                                "--GITHUB_CLIENT_SECRET=test-github-secret",
                                "--FACEBOOK_CLIENT_ID=test-facebook",
                                "--FACEBOOK_CLIENT_SECRET=test-facebook-secret",
                                "--TELEGRAM_CLIENT_ID=test-telegram",
                                "--TELEGRAM_CLIENT_SECRET=test-telegram-secret",
                                "--app.iam.clients.oidc.secret=test-client-secret",
                                "--IAM_DCR_CLIENT_SECRET=test-dcr-secret",
                                "--app.iam.signing-key-file="
                                        + (cluster == null
                                                        ? java.nio.file.Files.createTempDirectory("iam-test-key-")
                                                        : cluster)
                                                .resolve("signing-key.json"),
                                "--spring.main.banner-mode=off",
                                "--spring.datasource.url=" + url,
                                "--spring.datasource.username=" + username,
                                "--spring.datasource.password=" + password);
        jdbc = bean(JdbcTemplate.class);
    }

    @AfterAll
    static void stop() throws Exception {
        if (context != null) {
            context.close();
        }
        if (cluster != null) {
            try {
                command("pg_ctl", "-D", cluster.resolve("data").toString(), "-m", "fast", "-w", "stop");
            } finally {
                try (var paths = Files.walk(cluster)) {
                    for (var path : paths.sorted(Comparator.reverseOrder()).toList()) {
                        Files.deleteIfExists(path);
                    }
                }
            }
        }
    }

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext()
                .setAuthentication(UsernamePasswordAuthenticationToken.authenticated("test-actor", null, List.of()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    protected UserResult register() {
        var name = unique();
        return bean(RegisterUserUseCase.class)
                .execute(new RegisterUserCommand(name, name + "@example.com", PASSWORD, PASSWORD, "Dara", "Sok", "km"));
    }

    protected kh.edu.iam.application.permission.result.PermissionResult permission() {
        return bean(CreatePermissionUseCase.class).execute(new CreatePermissionCommand(unique(), "read", "Test"));
    }

    protected RoleResult role(List<UUID> permissions) {
        return bean(CreateRoleUseCase.class).execute(new CreateRoleCommand(unique(), "Test role", permissions));
    }

    protected FederatedIdentityCommand identity(IdentityProvider provider, String subject, String email) {
        return new FederatedIdentityCommand(
                provider,
                subject,
                email,
                email != null,
                "Dara Sok",
                "dara",
                "https://example.com/avatar.png",
                "{\"id\":\"provider-user\"}",
                "Dara",
                "Sok",
                "en");
    }
}
