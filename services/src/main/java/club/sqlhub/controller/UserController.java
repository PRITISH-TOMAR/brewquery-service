package club.sqlhub.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import club.sqlhub.entity.user.DTO.profile.*;
import club.sqlhub.service.UserService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /** Full profile card: name, contact, level, header stats. */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getProfile(
            @PathVariable Integer userId) {
        return userService.getProfile(userId);
    }

    /**
     * Submission aggregates: totals, difficulty breakdown,
     * favourite datasets, recent badges.
     * Query param {@code period}: all | year | month (default: all)
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<ApiResponse<UserStatsResponseDTO>> getStats(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "all") String period) {
        return userService.getStats(userId, period);
    }

    /**
     * Recent submissions list.
     * Query param {@code limit}: number of rows to return (default: 5)
     */
    @GetMapping("/{userId}/submissions")
    public ResponseEntity<ApiResponse<List<UserSubmissionDTO>>> getSubmissions(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "5") int limit) {
        return userService.getSubmissions(userId, limit);
    }

    /** Daily submission counts for the activity heatmap. */
    @GetMapping("/{userId}/heatmap")
    public ResponseEntity<ApiResponse<List<HeatmapEntryDTO>>> getHeatmap(
            @PathVariable Integer userId) {
        return userService.getHeatmap(userId);
    }

    /** Upload / replace the user's avatar. Accepts multipart/form-data with field "file". */
    @PutMapping(value = "/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @PathVariable Integer userId,
            @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(userId, file);
    }
}
