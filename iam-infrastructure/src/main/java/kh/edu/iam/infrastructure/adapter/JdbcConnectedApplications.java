package kh.edu.iam.infrastructure.adapter;

import kh.edu.iam.application.account.result.ConnectedAppResult;
import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.application.port.out.ConnectedApplications;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class JdbcConnectedApplications implements ConnectedApplications {
    private final JdbcOperations jdbc;
    private final RegisteredClientRepository clients;
    private final OAuth2AuthorizationService authorizations;
    private final OAuth2AuthorizationConsentService consents;

    @Override
    @Transactional(readOnly = true)
    public List<ConnectedAppResult> list(String username) {
        var ids =
                jdbc.queryForList(
                        "SELECT registered_client_id FROM oauth2_authorization_consent WHERE principal_name = ? "
                                + "UNION SELECT registered_client_id FROM oauth2_authorization WHERE principal_name = ?",
                        String.class,
                        username,
                        username);
        var result = new ArrayList<ConnectedAppResult>();
        for (String id : ids) {
            var client = clients.findById(id);
            if (client == null) {
                continue;
            }
            var scopes = new TreeSet<String>();
            var consent = consents.findById(id, username);
            if (consent != null) {
                scopes.addAll(consent.getScopes());
            }
            for (String authorizationId : authorizationIds(username, id)) {
                var authorization = authorizations.findById(authorizationId);
                if (authorization != null) {
                    scopes.addAll(authorization.getAuthorizedScopes());
                }
            }
            result.add(new ConnectedAppResult(id, client.getClientName(), scopes));
        }
        result.sort(Comparator.comparing(ConnectedAppResult::name).thenComparing(ConnectedAppResult::id));
        return List.copyOf(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ConnectedAppResult describe(String clientId, Set<String> requestedScopes) {
        var client = clients.findByClientId(clientId);
        if (client == null || !client.getScopes().containsAll(requestedScopes) || requestedScopes.isEmpty()) {
            throw new ApplicationException("invalid_request", "Unknown client or scopes");
        }
        return new ConnectedAppResult(client.getId(), client.getClientName(), requestedScopes);
    }

    @Override
    @Transactional
    public void revoke(String username, String applicationId) {
        var consent = consents.findById(applicationId, username);
        var ids = authorizationIds(username, applicationId);
        if (consent == null && ids.isEmpty()) {
            throw new ApplicationException("not_found", "Connected application not found");
        }
        for (String id : ids) {
            var authorization = authorizations.findById(id);
            if (authorization != null && username.equals(authorization.getPrincipalName())) {
                authorizations.remove(authorization);
            }
        }
        if (consent != null) {
            consents.remove(consent);
        }
    }

    private List<String> authorizationIds(String username, String clientId) {
        return jdbc.queryForList(
                "SELECT id FROM oauth2_authorization WHERE principal_name = ? AND registered_client_id = ?",
                String.class,
                username,
                clientId);
    }
}
