package kh.edu.iam;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.Instant;

class AssignmentRulesTest {
    private static final Instant START = Instant.parse("2026-11-01T00:00:00Z");

    @Test
    void assignmentIsActiveOnlyWithinItsTimeWindow() {
        var a = UserRole.assign(1L, 2L, "actor", START, START.plusSeconds(60), "Temporary");
        assertFalse(a.isActiveAt(START.minusNanos(1)));
        assertTrue(a.isActiveAt(START));
        assertTrue(a.isActiveAt(START.plusSeconds(59)));
        assertFalse(a.isActiveAt(START.plusSeconds(60)));
    }

    @Test
    void revocationPreservesOriginalAssignmentAndEndsAuthorityImmediately() {
        var a = RolePermission.assign(1L, 2L, "assigner", START, null, "Initial access");
        a.revoke("revoker", START.plusSeconds(10));
        assertTrue(a.isActiveAt(START.plusSeconds(9)));
        assertFalse(a.isActiveAt(START.plusSeconds(10)));
        assertEquals("assigner", a.getAssignedBy());
        assertEquals("Initial access", a.getReason());
        assertEquals("revoker", a.getRevokedBy());
        assertThrows(IllegalStateException.class, () -> a.revoke("again", START.plusSeconds(11)));
    }

    @Test
    void invalidExpiryAndIncompleteRevocationAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> UserRole.assign(1L, 2L, "actor", START, START, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new UserRole(1L, 1L, 2L, START, "actor", null, START, null, null));
    }

    @Test
    void systemRolesAndPermissionsAreProtected() {
        var audit = AuditMetadata.create("seed", START);
        var role =
                new Role(1L, java.util.UUID.randomUUID(), "ADMIN", null, true, true, audit, DeletionMetadata.active());
        var permission =
                new Permission(
                        1L,
                        java.util.UUID.randomUUID(),
                        "user",
                        "read",
                        null,
                        true,
                        true,
                        audit,
                        DeletionMetadata.active());
        assertThrows(IllegalStateException.class, () -> role.delete("actor", START));
        assertThrows(IllegalStateException.class, () -> permission.deactivate("actor", START));
    }
}
