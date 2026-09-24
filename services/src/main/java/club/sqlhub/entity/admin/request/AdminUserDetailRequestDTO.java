package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserDetailRequestDTO {

    @NotNull(message = "userId is required")
    private Integer userId;
}
