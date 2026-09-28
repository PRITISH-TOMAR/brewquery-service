package club.sqlhub.utils.access;

import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.Auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class AccessControlAspect {

    @Around("@annotation(requiresAccess)")
    public Object check(ProceedingJoinPoint pjp, RequiresAccess requiresAccess) throws Throwable {
        UserPrincipal principal = getPrincipal();
        if (principal == null)
            return ApiResponse.call(HttpStatus.UNAUTHORIZED, "Authentication required");

        String userRole = getUserRole(principal);

        if (requiresAccess.roles().length > 0) {
            boolean roleOk = Arrays.stream(requiresAccess.roles())
                    .anyMatch(r -> roleRank(userRole) >= roleRank(r));
            if (!roleOk)
                return ApiResponse.call(HttpStatus.FORBIDDEN, "Insufficient role");
        }

        if (requiresAccess.permissions().length > 0) {
            boolean permOk = Arrays.stream(requiresAccess.permissions())
                    .allMatch(p -> hasPermission(principal, p));
            if (!permOk)
                return ApiResponse.call(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }

        return pjp.proceed();
    }

    private UserPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal p)) return null;
        return p;
    }

    private String getUserRole(UserPrincipal p) {
        return p.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }

    private int roleRank(String role) {
        return switch (role.toUpperCase()) {
            case "SUPERADMIN" -> 3;
            case "ADMIN"      -> 2;
            case "USER"       -> 1;
            default           -> 0;
        };
    }

    private boolean hasPermission(UserPrincipal principal, String permission) {
        String[] parts = permission.split(":");
        if (parts.length != 2) return false;
        String module    = parts[0].toUpperCase();
        String operation = parts[1].toUpperCase();
        Set<String> ops  = principal.getModulePermissions().getOrDefault(module, Set.of());
        return ops.contains(operation);
    }
}
