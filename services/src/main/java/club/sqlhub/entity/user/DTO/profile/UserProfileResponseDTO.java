package club.sqlhub.entity.user.DTO.profile;

import lombok.Data;

@Data
public class UserProfileResponseDTO {
    private Integer      userId;
    private String       name;
    private String       role;
    private String       email;
    private String       phone;
    private String       joinedAt;   // "Nov 2025"
    private String       location;
    private String       bio;
    private String       avatar;
    private LevelDTO     level;
    private ProfileStatsDTO stats;
}
