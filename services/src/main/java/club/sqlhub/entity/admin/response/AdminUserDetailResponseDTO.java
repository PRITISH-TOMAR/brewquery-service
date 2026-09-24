package club.sqlhub.entity.admin.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminUserDetailResponseDTO {
    private Integer                    userId;
    private String                     name;
    private String                     email;
    private String                     role;
    private String                     status;
    private String                     createdAt;
    private List<AdminPermissionItemDTO> permissions;
}
