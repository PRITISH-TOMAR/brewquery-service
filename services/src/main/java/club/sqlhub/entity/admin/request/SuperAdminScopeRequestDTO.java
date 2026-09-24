package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SuperAdminScopeRequestDTO {

    @NotNull(message = "adminId is required")
    private Integer adminId;

    @NotBlank(message = "moduleKey is required")
    private String moduleKey;

    private List<String> grantableOps;
}
