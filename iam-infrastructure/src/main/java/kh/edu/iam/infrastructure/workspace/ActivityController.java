package kh.edu.iam.infrastructure.workspace;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.user.usecase.GetUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({"/workspace/api/activity", "/api/console/activity"})
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Workspace")
public class ActivityController {
    private static final String REPEATED_FAILURE = """
            (a.event = 'login' AND NOT a.success AND
                (SELECT count(*) FROM iam_activity previous
                 WHERE previous.subject = a.subject AND previous.event = 'login' AND NOT previous.success
                 AND previous.occurred_at BETWEEN a.occurred_at - INTERVAL '15 minutes' AND a.occurred_at
                 AND (previous.occurred_at < a.occurred_at OR previous.id <= a.id)) >= 5)
            """;

    private final WorkspaceAccess access;
    private final GetUserUseCase users;
    private final JdbcTemplate jdbc;

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Filter account activity and system audit logs")
    public PageResult<Map<String, Object>> activity(
            @RequestParam(name = "user", required = false) UUID user,
            @RequestParam(name = "audit", defaultValue = "false") boolean audit,
            @RequestParam(name = "event", defaultValue = "") String event,
            @RequestParam(name = "success", required = false) Boolean success,
            @RequestParam(name = "actor", required = false) UUID actor,
            @RequestParam(name = "target", defaultValue = "") String target,
            @RequestParam(name = "from", required = false) LocalDate from,
            @RequestParam(name = "to", required = false) LocalDate to,
            @RequestParam(name = "suspicious", defaultValue = "false") boolean suspicious,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        UUID subject = access.currentUser();
        if (audit) access.require("audit:read");
        if (user != null && !user.equals(subject)) {
            access.require("user:read");
            users.execute(user);
            subject = user;
        }
        if (page < 0 || page > Integer.MAX_VALUE / 100 || size < 1 || size > 100
                || event.length() > 200 || target.length() > 200
                || from != null && (from.getYear() < 1 || from.getYear() > 9999)
                || to != null && (to.getYear() < 1 || to.getYear() > 9999)
                || from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        var where = new StringBuilder(" WHERE a.event LIKE ? ESCAPE '!'");
        var args = new ArrayList<Object>();
        args.add(contains(event));
        if (!audit) {
            where.append(" AND a.subject = ?");
            args.add(subject.toString());
        }
        if (actor != null) {
            where.append(" AND a.subject = ?");
            args.add(actor.toString());
        }
        if (!target.isBlank()) {
            where.append(" AND a.target LIKE ? ESCAPE '!'");
            args.add(contains(target));
        }
        if (success != null) {
            where.append(" AND a.success = ?");
            args.add(success);
        }
        if (from != null) {
            where.append(" AND a.occurred_at >= ?");
            args.add(java.sql.Timestamp.from(from.atStartOfDay().toInstant(ZoneOffset.UTC)));
        }
        if (to != null) {
            where.append(" AND a.occurred_at < ?");
            args.add(java.sql.Timestamp.from(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        }
        if (suspicious) where.append(" AND ").append(REPEATED_FAILURE);
        long count = jdbc.queryForObject("SELECT count(*) FROM iam_activity a" + where,
                Long.class, args.toArray());
        args.add(size);
        args.add(page * size);
        String select = "SELECT a.id, a.subject, actor.username AS actor_username, a.target, a.event, a.success, a.ip, a.device, a.occurred_at, "
                + REPEATED_FAILURE + " AS suspicious FROM iam_activity a LEFT JOIN users actor ON actor.uuid::text = a.subject";
        var rows = jdbc.queryForList(select + where + " ORDER BY a.occurred_at DESC, a.id DESC LIMIT ? OFFSET ?",
                args.toArray());
        return new PageResult<>(rows, page, size, count, (int) ((count + size - 1) / size));
    }

    private static String contains(String value) {
        return "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }
}
