package kh.edu.iam.application.permission.command;

public record CreatePermissionCommand(String resource, String action, String description) {}
/**
 * /
 * The command carries input.
 * Your existing Permission domain model already validates resource,
 * action, and description.
 */
