package club.sqlhub.mongo.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "page_assets")
@Data
public class PageAssets {

    @Id
    private String id;

    @Indexed(unique = true)
    private String pageKey; // e.g. "sql", "nosql", "vectordb"

    private String heroImageUrl;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
