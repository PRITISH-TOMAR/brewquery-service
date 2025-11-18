package club.sqlhub.entity.user.DBO;

import java.security.Timestamp;

import club.sqlhub.constants.AppConstants;
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

    private String status = AppConstants.DEFAULT_STATUS;

    private Integer roleId = AppConstants.DEFAULT_ROLE_ID;

    private String phoneNumber;

    private String countryCode;

    private String profilePictureUrl = AppConstants.DEFAULT_PROFILE_PICTURE_PATH;

    private String hashedPassword;

    private String salt;

    private Timestamp lastLogin=null;

}
