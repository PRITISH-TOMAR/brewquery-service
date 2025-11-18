package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class AuthQueries {

    public final String ValidateUser = """
            UPDATE user_details
            SET status = "ACTIVE"
            WHERE
            email = ?;
            """;

}
