package kh.edu.iam.application.user.command;

import kh.edu.iam.PermissionEffect;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GrantPermissionsToUserCommand(
        UUID userUuid, List<UUID> permissionUuids, PermissionEffect effect, Instant expiresAt, String reason) {}
