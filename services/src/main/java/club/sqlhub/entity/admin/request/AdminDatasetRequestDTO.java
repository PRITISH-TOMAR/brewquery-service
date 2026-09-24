package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminDatasetRequestDTO {

    @NotBlank(message = "slug is required")
    private String slug;

    @NotBlank(message = "title is required")
    private String title;

    private String description;
    private String icon;
    private String difficulty;

    /** Must match a ModuleEnum value (SQL / NOSQL / VECTORDB). Used for permission check. */
    @NotBlank(message = "dataType is required")
    private String dataType;

    private String estimatedTime;
    private List<String> tags;
    private List<String> categories;
    private List<String> skills;
    private List<String> sqlModesAvailable;
    private int tableCount;
}
