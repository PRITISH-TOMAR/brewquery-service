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
    private List<String> sqlModesAvailable;
    private int questions;
    private int tableCount;
    private String dataType;
    private String estimatedTime;
    private String erImage;

    private LocalDateTime createdAt = LocalDateTime.now();
}
