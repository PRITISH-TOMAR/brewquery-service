package club.sqlhub.entity.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsDBO {
    private Integer userId;

    private String firstName;

    private String lastName;

    @Email(message = "Invalid Email Format")
    private String email;

    private String status;

    private Integer roleId = 1;

    private String phoneNumber;

    private String countryCode;

    private String profilePictureUrl = null;
}
