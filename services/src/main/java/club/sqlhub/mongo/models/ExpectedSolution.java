package club.sqlhub.mongo.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "expected_solutions")
@Data
public class ExpectedSolution {

    @Id
    private String id;

    private String questionId;
    private String datasetId;
    private String sqlMode;
    private String solutionQuery;

    private String resultHash;

    private LocalDateTime createdAt = LocalDateTime.now();
}
