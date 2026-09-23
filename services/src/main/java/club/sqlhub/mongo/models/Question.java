package club.sqlhub.mongo.models;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Document(collection = "questions")
@Data
public class Question {

    @Id
    private String id; 

    private String datasetId;
    private String title;
    private String question;
    private String difficulty;
    private List<String> tags;
    private String type;
    @JsonIgnore
    private List<String> tableNames;
    private LocalDateTime createdAt = LocalDateTime.now();
}
