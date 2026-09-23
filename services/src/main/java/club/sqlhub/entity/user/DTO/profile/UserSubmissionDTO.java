package club.sqlhub.entity.user.DTO.profile;

import lombok.Data;

@Data
public class UserSubmissionDTO {
    private String id;
    private String questionTitle;
    private String dataset;
    private String level;        // "Easy" | "Medium" | "Hard"
    private String language;     // "SQL"
    private String timeTaken;    // "2m 14s"
    private String submittedAt;  // "Sep 20, 2026 10:24 PM"
    private String result;       // "Accepted" | "Wrong Answer"
}
