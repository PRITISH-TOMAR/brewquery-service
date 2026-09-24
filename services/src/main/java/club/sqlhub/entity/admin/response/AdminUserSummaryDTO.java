package club.sqlhub.entity.admin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminUserSummaryDTO {
    private Integer userId;
    private String  name;
    private String  email;
    private String  role;
    private String  status;
    private String  createdAt;
}
