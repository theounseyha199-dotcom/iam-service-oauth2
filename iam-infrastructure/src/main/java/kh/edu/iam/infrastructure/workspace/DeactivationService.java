package kh.edu.iam.infrastructure.workspace;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import kh.edu.iam.application.user.usecase.DisableUserUseCase;
import kh.edu.iam.infrastructure.security.IamUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DeactivationService {
    public enum Decision { APPROVED, REJECTED }

    private final JdbcTemplate jdbc;
    private final WorkspaceAccess access;
    private final DisableUserUseCase disableUser;
    private final SessionRegistry sessions;

    @Transactional
    public void request(String reason) {
        int changed = jdbc.update("""
                INSERT INTO iam_deactivation_request(subject, reason) VALUES (?, ?)
                ON CONFLICT(subject) DO UPDATE SET reason = EXCLUDED.reason,
                    requested_at = CURRENT_TIMESTAMP, status = 'PENDING', reviewed_by = NULL,
                    reviewed_at = NULL, review_note = NULL, version = iam_deactivation_request.version + 1
                WHERE iam_deactivation_request.status <> 'APPROVED'
                """, access.currentUser(), reason.strip());
        if (changed == 0) throw new ResponseStatusException(HttpStatus.CONFLICT);
    }

    public Map<String, Object> mine() {
        var rows = jdbc.queryForList("""
                SELECT reason, requested_at, status, reviewed_at, review_note, version
                FROM iam_deactivation_request WHERE subject = ?
                """, access.currentUser());
        if (rows.isEmpty()) return Map.of("pending", false);
        var result = new java.util.LinkedHashMap<>(rows.getFirst());
        result.put("pending", "PENDING".equals(result.get("status")));
        return result;
    }

    public List<Map<String, Object>> list(String status) {
        access.require("user:read");
        if (!java.util.Set.of("", "PENDING", "APPROVED", "REJECTED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return jdbc.queryForList("""
                SELECT d.subject, d.reason, d.requested_at, d.status, d.reviewed_by,
                       d.reviewed_at, d.review_note, d.version, u.username
                FROM iam_deactivation_request d JOIN users u ON u.uuid = d.subject
                WHERE (? = '' OR d.status = ?)
                ORDER BY d.requested_at DESC, d.subject LIMIT 100
                """, status, status);
    }

    @Transactional
    public void review(UUID subject, Decision decision, String reason, long version) {
        UUID reviewer = access.require("user:update");
        access.require("user:read");
        if (reviewer.equals(subject)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        var requests = jdbc.queryForList(
                "SELECT status, version FROM iam_deactivation_request WHERE subject = ? FOR UPDATE", subject);
        if (requests.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var request = requests.getFirst();
        if (!"PENDING".equals(request.get("status"))
                || ((Number) request.get("version")).longValue() != version) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        if (decision == Decision.APPROVED) {
            disableUser.execute(subject);
            jdbc.update("""
                    DELETE FROM oauth2_authorization WHERE principal_name = ?
                    OR principal_name = (SELECT username FROM users WHERE uuid = ?)
                    """, subject.toString(), subject);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (Object principal : sessions.getAllPrincipals()) {
                        if (principal instanceof IamUserPrincipal user && user.uuid().equals(subject)) {
                            sessions.getAllSessions(principal, false).forEach(session -> session.expireNow());
                        }
                    }
                }
            });
        }
        jdbc.update("""
                UPDATE iam_deactivation_request SET status = ?, reviewed_by = ?,
                    reviewed_at = CURRENT_TIMESTAMP, review_note = ?, version = version + 1
                WHERE subject = ?
                """, decision.name(), reviewer, reason.strip(), subject);
        jdbc.update("INSERT INTO iam_activity(subject, event, success, target) VALUES (?, ?, TRUE, ?)",
                reviewer.toString(), "deactivation_" + decision.name().toLowerCase(java.util.Locale.ROOT),
                "/users/" + subject);
    }
}
