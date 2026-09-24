package club.sqlhub.entity.admin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminPermissionItemDTO {
    private String  moduleKey;
    private String  operation;
    private String  source;
    private boolean granted;
    private String  expiresAt;
}
