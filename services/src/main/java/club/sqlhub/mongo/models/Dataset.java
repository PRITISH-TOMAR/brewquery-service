package club.sqlhub.mongo.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class Dataset {

    private String id;

    private String slug;
    private String title;
    private String description;
    private String icon;
    private String coverImage;
    private List<String> tags;
    private List<String> categories;
    private List<String> skills;
    private String difficulty;
    private List<String> modesAvailable;
    private int questions;
    private int tableCount;
    private List<String> tableNames;
    private String dataType;
    private String estimatedTime;
    private String erImage;
    private int active;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime deletedAt;
}
