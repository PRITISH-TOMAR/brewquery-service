package club.sqlhub.Repository;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import club.sqlhub.entity.user.DBO.UserDetailsDBO;
import club.sqlhub.entity.user.DTO.UserDetailsDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepository {
    private final JdbcTemplate jdbc;

    public List<UserDetailsDBO> userExists(String email, String query) {
        List<UserDetailsDBO> userExists = jdbc.query(query, new BeanPropertyRowMapper<>(UserDetailsDBO.class),
                email);
        return userExists;
    }

    public UserDetailsDTO addUser(UserDetailsDBO user, String addQuery) {
        return jdbc.queryForObject(
                addQuery,
                new BeanPropertyRowMapper<>(UserDetailsDTO.class),
                user.getFirstName(), user.getLastName(), user.getEmail(), user.getStatus(),
                user.getRoleId(), user.getPhoneNumber(), user.getCountryCode(), user.getProfilePictureUrl(),
                user.getHashedPassword(), user.getSalt());
    }

}
