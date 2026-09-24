package club.sqlhub.entity.admin.request;

import club.sqlhub.mongo.models.ExpectedSolution.SolutionEntry;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminExpectedSolutionRequestDTO {

    @NotBlank(message = "questionId is required")
    private String questionId;

    @NotBlank(message = "datasetId is required")
    private String datasetId;

    private String sqlMode;
    private List<SolutionEntry> solutions;
}
