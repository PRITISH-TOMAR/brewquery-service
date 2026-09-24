package club.sqlhub.entity.admin.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminScopeSummaryDTO {
    private Integer              userId;
    private String               name;
    private String               email;
    private String               status;
    private List<AdminScopeItemDTO> scopes;
}
