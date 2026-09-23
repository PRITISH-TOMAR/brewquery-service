package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class UserProfileQueries {

    public final String FIND_PROFILE_BY_ID = """
            SELECT
                ud.user_id             AS userId,
                ud.first_name          AS firstName,
                ud.last_name           AS lastName,
                ud.email               AS email,
                ud.phone_number        AS phoneNumber,
                ud.country_code        AS countryCode,
                ud.profile_picture_url AS profilePictureUrl,
                ud.created_at          AS createdAt,
                ur.role_name           AS roleName
            FROM user_details ud
            JOIN user_roles   ur ON ud.role_id = ur.role_id
            WHERE ud.user_id = ?
            """;

    public final String UPDATE_AVATAR_URL = """
            UPDATE user_details SET profile_picture_url = ? WHERE user_id = ?
            """;

    public final String FIND_BADGES_BY_USER_ID = """
            SELECT
                bd.id::TEXT    AS id,
                bd.name        AS name,
                bd.description AS description,
                bd.color       AS color
            FROM user_badges        ub
            JOIN badge_definitions  bd ON ub.badge_id = bd.id
            WHERE ub.user_id = ?
            ORDER BY ub.earned_at DESC
            LIMIT 5
            """;
}
