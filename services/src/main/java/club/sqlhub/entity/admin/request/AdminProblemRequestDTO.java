package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminProblemRequestDTO {

    private String datasetId; // set server-side from path variable

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "question (problem statement) is required")
    private String question;

    @NotBlank(message = "difficulty is required")
    private String difficulty;

    private String type;

    @jakarta.validation.constraints.NotNull(message = "tags are required")
    @jakarta.validation.constraints.Size(min = 1, message = "at least one tag is required")
    private List<String> tags;

    private List<String> tableNames;
}
