package club.sqlhub.utils.Auth;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final String userId;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    /** Active module permissions loaded at auth time. Key = moduleKey (e.g. "SQL"), value = set of operations (e.g. {"READ","WRITE"}). */
    private final Map<String, Set<String>> modulePermissions;

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
