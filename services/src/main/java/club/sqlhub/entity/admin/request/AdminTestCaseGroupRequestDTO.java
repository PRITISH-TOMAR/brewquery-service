package club.sqlhub.entity.admin.request;

import club.sqlhub.mongo.models.TestCaseSQL.TestCase;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminTestCaseGroupRequestDTO {

    @NotBlank(message = "questionId is required")
    private String questionId;

    private String type;
    private String expectedSql;
    private List<TestCase> testCases;
}
