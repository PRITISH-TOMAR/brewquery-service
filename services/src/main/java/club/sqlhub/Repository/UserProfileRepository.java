package club.sqlhub.Repository;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import club.sqlhub.entity.user.DBO.UserRawProfileDBO;
import club.sqlhub.entity.user.DTO.profile.BadgeDTO;
import club.sqlhub.queries.UserProfileQueries;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserProfileRepository {

    private final JdbcTemplate        jdbc;
    private final UserProfileQueries  queries;

    /**
     * Returns the full user profile row (user_details JOIN user_roles).
     * Returns null when no user exists for the given id.
     */
    public UserRawProfileDBO findProfileById(Integer userId) {
        try {
            return jdbc.queryForObject(
                    queries.FIND_PROFILE_BY_ID,
                    new BeanPropertyRowMapper<>(UserRawProfileDBO.class),
                    userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Updates profile_picture_url for the given user.
     */
    public void updateProfilePictureUrl(Integer userId, String url) {
        jdbc.update(queries.UPDATE_AVATAR_URL, url, userId);
    }

    /**
     * Returns up to 5 most recently earned badges for the user.
     * Returns empty list if the badges tables don't exist yet.
     */
    public List<BadgeDTO> findBadgesByUserId(Integer userId) {
        try {
            return jdbc.query(
                    queries.FIND_BADGES_BY_USER_ID,
                    new BeanPropertyRowMapper<>(BadgeDTO.class),
                    userId);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
