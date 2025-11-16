package club.sqlhub.Repository;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import club.sqlhub.entity.user.UserDetailsDBO;
import club.sqlhub.queries.UserQueries;

@Component
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc, UserQueries queries) {
        this.jdbc = jdbc;
    }

    public List<UserDetailsDBO> userExists(UserDetailsDBO user, String query) {
        List<UserDetailsDBO> userExists = jdbc.query(query, new BeanPropertyRowMapper<>(UserDetailsDBO.class),
                user.getEmail());
        return userExists;
    }

    public UserDetailsDBO addUser(UserDetailsDBO user, String addQuery, String getUserQuery) {
        jdbc.update(addQuery, user.getFirstName(), user.getLastName(), user.getEmail(), user.getRoleId(),
                user.getPhoneNumber(), user.getCountryCode(), user.getProfilePictureUrl());

        UserDetailsDBO createdUser = jdbc.queryForObject(getUserQuery,
                new BeanPropertyRowMapper<>(UserDetailsDBO.class));
        return createdUser;

    }

}
