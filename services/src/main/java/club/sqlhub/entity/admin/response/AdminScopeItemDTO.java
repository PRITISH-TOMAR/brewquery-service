package club.sqlhub.entity.admin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AdminScopeItemDTO {
    private String       moduleKey;
    private List<String> grantableOps;
}
