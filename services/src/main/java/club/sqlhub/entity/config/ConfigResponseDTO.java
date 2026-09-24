package club.sqlhub.entity.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigResponseDTO {
    private Integer userId;
    private String role;
    private List<ModuleConfigDTO> modules;
    private List<AdminScopeDTO> adminScope;
}
