package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminPermissionsRequestDTO {

    @NotNull(message = "userId is required")
    private Integer userId;

    private List<ModulePermissionEntry> grants;

    private List<ModulePermissionEntry> revokes;

    @Getter
    @Setter
    public static class ModulePermissionEntry {
        private String moduleKey;
        private List<String> operations;
    }
}
