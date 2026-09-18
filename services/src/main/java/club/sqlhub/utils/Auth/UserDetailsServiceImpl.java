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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        List<UserDetailsDBO> userList = userRepository.userExists(email, queries.IF_USER_EXISTS);

        if (userList.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + email);
        }

        UserDetailsDBO user = userList.get(0);

        // Role mapping → ROLE_USER / ROLE_ADMIN etc
        String role = (user.getRoleId() == 1) ? "ROLE_USER" : "ROLE__";

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getHashedPassword()) // not used for JWT flow
                .roles(role.replace("ROLE_", "")) // Spring auto adds ROLE_
                .build();
    }
}
