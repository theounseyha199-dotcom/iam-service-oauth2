package kh.edu.iam.infrastructure.workspace;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PasswordHasher;
import kh.edu.iam.application.user.usecase.GetUserAuthoritiesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@io.swagger.v3.oas.annotations.tags.Tag(name="OAuth Clients")
@RestController
@RequestMapping({"/workspace/api/clients","/api/console/clients"})
@RequiredArgsConstructor
public class WorkspaceClients {
    private final RegisteredClientRepository clients;
    private final CurrentActorProvider actors;
    private final GetUserAuthoritiesUseCase authorities;
    private final org.springframework.security.crypto.password.PasswordEncoder passwords;
    private final JdbcTemplate jdbc;
    private final WorkspaceController workspace;
    private void require(String permission) {
        try {
            if(!authorities.execute(UUID.fromString(actors.currentActor())).effectivePermissions().contains(permission)) throw new AccessDeniedException("Forbidden");
        } catch(IllegalArgumentException ex) { throw new AccessDeniedException("User required"); }
    }
    public record Input(@NotBlank @Size(max=200) String clientId,@NotBlank @Size(max=200) String clientName,
            @Size(max=255) String clientSecret, boolean confidential,
            @NotEmpty @Size(max=3) Set<String> grantTypes,@Size(max=20) Set<String> redirectUris,
            @NotEmpty @Size(max=30) Set<String> scopes,
            @Min(1) @Max(60) Integer accessMinutes,@Min(5) @Max(43200) Integer refreshMinutes) {
        @Override public String toString() { return "ClientInput[redacted]"; }
    }
    private Map<String,Object> view(RegisteredClient c) {
        return Map.of("uuid",c.getId(),"clientId",c.getClientId(),"clientName",c.getClientName(),
                "confidential",!c.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE),
                "grantTypes",c.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).toList(),
                "redirectUris",c.getRedirectUris(),"scopes",c.getScopes(),
                "accessMinutes",c.getTokenSettings().getAccessTokenTimeToLive().toMinutes(),
                "refreshMinutes",c.getTokenSettings().getRefreshTokenTimeToLive().toMinutes());
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Read OAuth clients")
    @GetMapping public kh.edu.iam.application.common.PageResult<Map<String,Object>> list(
            @RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam(name="search",defaultValue="") String search) {
        require("client:read"); if(page<0 || size<1 || size>100 || search.length()>200) throw new IllegalArgumentException();
        String like="%"+search+"%";
        long count=jdbc.queryForObject("SELECT count(*) FROM oauth2_registered_client WHERE client_id ILIKE ? OR client_name ILIKE ?",Long.class,like,like);
        var ids=jdbc.queryForList("SELECT id FROM oauth2_registered_client WHERE client_id ILIKE ? OR client_name ILIKE ? ORDER BY client_id LIMIT ? OFFSET ?",String.class,like,like,size,Math.multiplyExact(page,size));
        return new kh.edu.iam.application.common.PageResult<>(ids.stream().map(clients::findById).filter(Objects::nonNull).map(this::view).toList(),page,size,count,(int)((count+size-1)/size));
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Read OAuth clients")
    @GetMapping("/{id}") public Map<String,Object> get(@PathVariable("id") String id) { require("client:read"); return view(find(id)); }
    private RegisteredClient find(String id) { var c=clients.findById(id); if(c==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); return c; }
    @io.swagger.v3.oas.annotations.Operation(summary="Create OAuth clients")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> create(@Valid @RequestBody Input input) {
        require("client:create"); return save(null,input);
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Update OAuth clients")
    @PutMapping("/{id}") public Map<String,Object> update(@PathVariable("id") String id,@Valid @RequestBody Input input) {
        require("client:update"); return save(find(id),input);
    }
    private Map<String,Object> save(RegisteredClient previous,Input input) {
        if (!Set.of("authorization_code","refresh_token","client_credentials").containsAll(input.grantTypes())) throw new IllegalArgumentException();
        if(input.grantTypes().contains("refresh_token") && !input.grantTypes().contains("authorization_code")) throw new IllegalArgumentException();
        if(!input.confidential() && input.grantTypes().contains("client_credentials")) throw new IllegalArgumentException();
        if(input.grantTypes().contains("authorization_code") && (input.redirectUris()==null || input.redirectUris().isEmpty())) throw new IllegalArgumentException();
        if(input.redirectUris()!=null) for(String value:input.redirectUris()) {
            URI uri=URI.create(value);
            boolean local=Set.of("localhost","127.0.0.1","[::1]").contains(Objects.toString(uri.getHost(),""));
            if(value.length()>500 || uri.getHost()==null || uri.getUserInfo()!=null || uri.getFragment()!=null || !("https".equals(uri.getScheme()) || local && "http".equals(uri.getScheme()))) throw new IllegalArgumentException();
        }
        if(input.scopes().stream().anyMatch(s->!s.matches("[A-Za-z0-9_.:-]{1,100}"))) throw new IllegalArgumentException();
        var policy=workspace.policyValue();
        var builder=previous==null?RegisteredClient.withId(UUID.randomUUID().toString()).clientIdIssuedAt(Instant.now()):RegisteredClient.from(previous);
        builder.clientId(input.clientId()).clientName(input.clientName());
        builder.clientAuthenticationMethods(set->{set.clear();set.add(input.confidential()?ClientAuthenticationMethod.CLIENT_SECRET_BASIC:ClientAuthenticationMethod.NONE);});
        if(input.confidential()) {
            if(input.clientSecret()!=null && !input.clientSecret().isBlank()) {
                if(input.clientSecret().length()<24 || input.clientSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw new IllegalArgumentException();
                builder.clientSecret(passwords.encode(input.clientSecret()));
            } else if(previous==null || previous.getClientSecret()==null) throw new IllegalArgumentException();
        } else builder.clientSecret(null);
        builder.authorizationGrantTypes(set->{set.clear(); input.grantTypes().forEach(g->set.add(new AuthorizationGrantType(g)));});
        builder.redirectUris(set->{set.clear();if(input.redirectUris()!=null)set.addAll(input.redirectUris());});
        builder.scopes(set->{set.clear();set.addAll(input.scopes());});
        builder.clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(policy.requireConsent()).build());
        var token=previous==null?TokenSettings.builder():TokenSettings.withSettings(previous.getTokenSettings().getSettings());
        builder.tokenSettings(token.accessTokenTimeToLive(Duration.ofMinutes(input.accessMinutes()==null?policy.accessMinutes():input.accessMinutes()))
                .refreshTokenTimeToLive(Duration.ofMinutes(input.refreshMinutes()==null?policy.refreshMinutes():input.refreshMinutes())).reuseRefreshTokens(false).build());
        var client=builder.build(); clients.save(client);return view(client);
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Remove OAuth clients")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
    public void delete(@PathVariable("id") String id) {
        require("client:delete");find(id);
        jdbc.update("DELETE FROM oauth2_authorization_consent WHERE registered_client_id=?",id);
        jdbc.update("DELETE FROM oauth2_authorization WHERE registered_client_id=?",id);
        jdbc.update("DELETE FROM oauth2_registered_client WHERE id=?",id);
    }
}
