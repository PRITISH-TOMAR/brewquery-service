package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRoleRequestDTO {

    @NotNull(message = "userId is required")
    private Integer userId;

    @NotBlank(message = "newRole is required")
    private String newRole; // USER | ADMIN
}
