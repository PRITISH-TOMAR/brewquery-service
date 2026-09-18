package club.sqlhub.mongo.models;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "datasets")
@Data
public class Dataset {

    @Id
    private String id;

    private String title;
    private String description;
    private List<String> tags;
    private String difficulty;
    private List<String> sqlModesAvailable;

    private LocalDateTime createdAt = LocalDateTime.now();
}
