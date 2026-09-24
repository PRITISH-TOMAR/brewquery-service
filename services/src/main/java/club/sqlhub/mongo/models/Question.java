package club.sqlhub.mongo.models;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Question {

    private String id;

    private String datasetId;
    private String title;
    private String question;
    private String difficulty;
    private List<String> tags;
    private String type;
    @JsonIgnore
    private List<String> tableNames;
    private LocalDateTime createdAt  = LocalDateTime.now();
    private LocalDateTime deletedAt;
}
