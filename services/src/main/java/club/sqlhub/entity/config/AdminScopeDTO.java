package club.sqlhub.entity.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminScopeDTO {
    private String moduleKey;
    private List<String> grantableOperations;
}
