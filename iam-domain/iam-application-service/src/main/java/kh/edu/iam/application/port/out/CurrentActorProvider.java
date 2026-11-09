package kh.edu.iam.application.port.out;
/**
 The actor is an identifier used in audit fields,
 normally the authenticated user’s UUID.
 Public registration will need a defined actor such as "anonymous".
 */
public interface CurrentActorProvider {
    String currentActor();
}
