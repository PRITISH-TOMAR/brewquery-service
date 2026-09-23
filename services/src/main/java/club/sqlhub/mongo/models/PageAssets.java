package club.sqlhub.mongo.models;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PageAssets {

    private String id;

    private String pageKey; // e.g. "sql", "nosql", "vectordb"

    private String heroImageUrl;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
