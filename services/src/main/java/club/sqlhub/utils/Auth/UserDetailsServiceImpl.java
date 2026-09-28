package club.sqlhub.utils.Auth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import club.sqlhub.Repository.ConfigRepository;
import club.sqlhub.Repository.UserRepository;
import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.queries.UserQueries;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository   userRepository;
    private final UserQueries      queries;
    private final ConfigRepository configRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        List<UserDetailsDBO> userList = userRepository.userExistsById(Integer.parseInt(userId), queries.IF_USER_EXISTS_BY_ID);

        if (userList.isEmpty())
            throw new UsernameNotFoundException("User not found: " + userId);

        UserDetailsDBO user = userList.get(0);

        var authorities = org.springframework.security.core.authority.AuthorityUtils
                .createAuthorityList("ROLE_" + user.getRole().toUpperCase());

        List<Map<String, Object>> permRows = configRepository.findActivePermissions(user.getUserId());
        Map<String, Set<String>> modulePermissions = new HashMap<>();
        for (Map<String, Object> row : permRows) {
            String moduleKey  = (String) row.get("moduleKey");
            String operation  = (String) row.get("operation");
            modulePermissions.computeIfAbsent(moduleKey, k -> new HashSet<>()).add(operation);
        }

        return new UserPrincipal(String.valueOf(user.getUserId()), user.getHashedPassword(),
                authorities, modulePermissions);
    }
}
