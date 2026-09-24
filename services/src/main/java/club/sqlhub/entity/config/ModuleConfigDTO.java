package club.sqlhub.entity.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ModuleConfigDTO {
    private String key;
    private boolean enabled;
    private List<String> operations;
    private SubscriptionDTO subscription;
}
