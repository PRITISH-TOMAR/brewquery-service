package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserStatusRequestDTO {

    @NotNull(message = "userId is required")
    private Integer userId;

    @NotBlank(message = "status is required")
    private String status; // ACTIVE | BLOCKED
}
