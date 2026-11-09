package kh.edu.iam.application.user.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssignRolesToUserCommand(UUID userUuid, List<UUID> roleUuids, Instant expiresAt, String reason) {}
