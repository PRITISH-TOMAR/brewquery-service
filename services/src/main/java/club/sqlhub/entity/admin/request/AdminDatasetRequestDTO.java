package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminDatasetRequestDTO {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    @NotBlank(message = "estimatedTime is required")
    private String estimatedTime;

    @NotBlank(message = "difficulty is required")
    private String difficulty;

    /** Set server-side from the {module} path variable — do not send in body. */
    private String dataType;

    private String icon;

    @NotNull(message = "tags are required")
    @Size(min = 1, message = "at least one tag is required")
    private List<String> tags;

    private List<String> categories;

    @NotNull(message = "skills are required")
    @Size(min = 1, message = "at least one skill is required")
    private List<String> skills;

    private List<String> modesAvailable;

    @NotNull(message = "tableNames are required")
    @Size(min = 1, message = "at least one table name is required")
    private List<String> tableNames;

    /** 0 = inactive (default), 1 = active (visible to regular users). */
    private int active = 0;
}
