package kh.edu.iam.infrastructure.workspace;

import java.util.UUID;

/** A completed security operation, emitted only after its owning transaction succeeds. */
public record AccountSecurityEvent(UUID subject, String action) {}
