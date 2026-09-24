package club.sqlhub.entity.admin.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminQuestionRequestDTO {

    @NotBlank(message = "datasetId is required")
    private String datasetId;

    @NotBlank(message = "title is required")
    private String title;

    private String question;
    private String difficulty;
    private String type;
    private List<String> tags;
    private List<String> tableNames;
}
