package club.sqlhub.entity.user.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {

    @NotBlank(message = "Reset key is required")
    private String key;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Getter
    @Setter
    public static class ForgotPasswordDTO {
        @Email(message = "Enter Valid Email")
        private String email;
    }
}
