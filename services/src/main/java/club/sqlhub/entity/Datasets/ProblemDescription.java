package club.sqlhub.entity.Datasets;

import club.sqlhub.mongo.models.Metadata;
import club.sqlhub.mongo.models.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProblemDescription {
    private Metadata metadata;
    private Question question;
}
