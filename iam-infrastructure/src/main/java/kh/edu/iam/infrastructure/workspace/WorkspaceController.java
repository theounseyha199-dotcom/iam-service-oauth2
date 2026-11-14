package kh.edu.iam.infrastructure.workspace;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.user.usecase.GetUserAuthoritiesUseCase;
import kh.edu.iam.application.user.usecase.GetUserUseCase;
import kh.edu.iam.infrastructure.security.IamUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@io.swagger.v3.oas.annotations.tags.Tag(name="Workspace")
@RestController
@RequestMapping({"/workspace/api", "/api/console"})
@RequiredArgsConstructor
public class WorkspaceController {
    private final CurrentActorProvider actors;
    private final GetUserAuthoritiesUseCase authorities;
    private final GetUserUseCase users;
    private final SessionRegistry sessions;
    private final JdbcTemplate jdbc;
    private final SessionLocation locations;

    private UUID me() {
        try {
            UUID id=UUID.fromString(actors.currentActor());
            var user=users.execute(id);
            if (!user.enabled() || !user.accountNonLocked() || !user.accountNonExpired() || !user.credentialsNonExpired()) throw new AccessDeniedException("Account unavailable");
            return id;
        } catch (IllegalArgumentException ex) { throw new AccessDeniedException("User required"); }
    }
    private void require(String permission) {
        if (!authorities.execute(me()).effectivePermissions().contains(permission)) throw new AccessDeniedException("Forbidden");
    }
    private UUID target(UUID user, String permission) {
        UUID id=me(); if (user!=null && !user.equals(id)) { require(permission); users.execute(user); return user; } return id;
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Read workspace identity")
    @GetMapping("/identity") public Map<String,Object> identity() {
        UUID id=me(); return Map.of("user",users.execute(id),"permissions",authorities.execute(id).effectivePermissions());
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Read workspace users/{user}/profile")
    @GetMapping("/users/{user}/profile") public kh.edu.iam.application.user.result.UserResult profile(@PathVariable("user") UUID user) {
        require("user:read"); return users.execute(user);
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Read new client defaults")
    @GetMapping("/client-defaults") public Map<String,Object> clientDefaults() {
        require("client:create");var p=policyValue();
        return Map.of("accessMinutes",p.accessMinutes(),"refreshMinutes",p.refreshMinutes());
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Read workspace overview")
    @GetMapping("/overview") public Map<String,Object> overview() {
        var allowed=authorities.execute(me()).effectivePermissions();
        var result=new LinkedHashMap<String,Object>();
        if(allowed.contains("user:read")) {
            result.put("users",jdbc.queryForObject("SELECT count(*) FROM users WHERE NOT is_deleted",Long.class));
            result.put("activeUsers",jdbc.queryForObject("SELECT count(*) FROM users WHERE NOT is_deleted AND enabled",Long.class));
        }
        if(allowed.contains("role:read")) result.put("roles",jdbc.queryForObject("SELECT count(*) FROM roles WHERE NOT is_deleted",Long.class));
        if(allowed.contains("permission:read")) result.put("permissions",jdbc.queryForObject("SELECT count(*) FROM permissions WHERE NOT is_deleted",Long.class));
        if(allowed.contains("client:read")) result.put("clients",jdbc.queryForObject("SELECT count(*) FROM oauth2_registered_client",Long.class));
        return result;
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Read workspace sessions")
    @GetMapping("/sessions") public List<Map<String,Object>> sessions(@RequestParam(name="user",required=false) UUID user, HttpServletRequest request) {
        UUID id=target(user,"user:read");
        var current=request.getSession(false);
        var result=new ArrayList<Map<String,Object>>();
        for(Object principal:sessions.getAllPrincipals()) {
            if (!(principal instanceof IamUserPrincipal p) || !p.uuid().equals(id)) continue;
            for(var session:sessions.getAllSessions(principal,false)) {
                String key=WorkspaceActivity.key(session.getSessionId());
                var row=new LinkedHashMap<String,Object>();
                row.put("id",key); row.put("lastActivity",session.getLastRequest().toInstant());
                row.put("current",current!=null && current.getId().equals(session.getSessionId()));
                var metadata=jdbc.queryForList("SELECT ip,device,created_at FROM iam_session_metadata WHERE session_key=?",key);
                if(!metadata.isEmpty()) row.putAll(metadata.getFirst());
                row.put("location", locations.lookup((String) row.get("ip")));
                result.add(row);
            }
        }
        return result;
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Remove workspace sessions/{key}")
    @DeleteMapping("/sessions/{key}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void terminate(@PathVariable("key") String key,@RequestParam(name="user",required=false) UUID user,HttpServletRequest request) {
        UUID id=target(user,"user:update"); boolean found=false;
        for(Object principal:sessions.getAllPrincipals()) {
            if (!(principal instanceof IamUserPrincipal p) || !p.uuid().equals(id)) continue;
            for(var session:sessions.getAllSessions(principal,false)) {
                if(key.equals("all") || key.equals(WorkspaceActivity.key(session.getSessionId()))) {
                    session.expireNow(); found=true;
                    var current=request.getSession(false);
                    if(current!=null && current.getId().equals(session.getSessionId())) current.invalidate();
                }
            }
        }
        if(!found && !key.equals("all")) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    @io.swagger.v3.oas.annotations.Operation(summary="Read assigned role permissions")
    @GetMapping("/roles/{role}/permissions") public List<Map<String,Object>> rolePermissions(@PathVariable("role") UUID role) {
        require("role:read");
        return jdbc.queryForList("SELECT p.uuid,p.name,rp.expires_at FROM role_permissions rp JOIN roles r ON r.id=rp.role_id JOIN permissions p ON p.id=rp.permission_id WHERE r.uuid=? AND NOT r.is_deleted AND NOT p.is_deleted AND rp.revoked_at IS NULL AND (rp.expires_at IS NULL OR rp.expires_at>CURRENT_TIMESTAMP) ORDER BY p.name",role);
    }
    public record Policy(@Min(5) @Max(1440) int sessionMinutes,@Min(1) @Max(60) int accessMinutes,
            @Min(5) @Max(43200) int refreshMinutes,@Min(8) @Max(64) int passwordMinimum,
            boolean requireNumber, boolean requireUppercase, boolean requireConsent,@Min(0) long version) {}
    @io.swagger.v3.oas.annotations.Operation(summary="Read workspace policy")
    @GetMapping("/policy") public Policy policy() { require("policy:read"); return policyValue(); }
    public Policy policyValue() { return jdbc.queryForObject("SELECT * FROM iam_workspace_policy WHERE id=1",(r,n)->new Policy(r.getInt("session_minutes"),r.getInt("access_minutes"),r.getInt("refresh_minutes"),r.getInt("password_minimum"),r.getBoolean("require_number"),r.getBoolean("require_uppercase"),r.getBoolean("require_consent"),r.getLong("version"))); }
    @io.swagger.v3.oas.annotations.Operation(summary="Update workspace policy")
    @PutMapping("/policy") public Policy policy(@Valid @RequestBody Policy input) {
        require("policy:update");
        int changed=jdbc.update("UPDATE iam_workspace_policy SET session_minutes=?,access_minutes=?,refresh_minutes=?,password_minimum=?,require_number=?,require_uppercase=?,require_consent=?,version=version+1 WHERE id=1 AND version=?",input.sessionMinutes(),input.accessMinutes(),input.refreshMinutes(),input.passwordMinimum(),input.requireNumber(),input.requireUppercase(),input.requireConsent(),input.version());
        if(changed==0) throw new ResponseStatusException(HttpStatus.CONFLICT);
        return policyValue();
    }
}
