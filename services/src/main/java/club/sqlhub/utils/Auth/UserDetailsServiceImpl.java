package club.sqlhub.utils.Auth;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import club.sqlhub.Repository.UserRepository;
import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.queries.UserQueries;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserQueries queries;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        List<UserDetailsDBO> userList = userRepository.userExistsById(Integer.parseInt(userId), queries.IF_USER_EXISTS_BY_ID);

        if (userList.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + userId);
        }

        UserDetailsDBO user = userList.get(0);

        String role = "ROLE_" + user.getRoleName().toUpperCase();

        var authorities = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(role);
        return new UserPrincipal(String.valueOf(user.getUserId()), user.getHashedPassword(), authorities);
    }
}
