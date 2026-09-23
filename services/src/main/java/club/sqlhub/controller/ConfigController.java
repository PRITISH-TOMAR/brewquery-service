package club.sqlhub.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.sqlhub.constants.MessageConstants;
import club.sqlhub.Repository.PageAssetsSQLRepository;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/config")
public class ConfigController {

    private final PageAssetsSQLRepository pageAssetsRepository;

    /** Frontend uses this for difficulty level labels and colours. */
    @GetMapping("/dataset-grid")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDatasetGridConfig() {
        Map<String, Object> config = Map.of(
                "difficultyLevels", List.of("Easy", "Medium", "Advanced"),
                "difficultyColor", Map.of(
                        "easy",     Map.of("color", "success", "variant", "filled"),
                        "medium",   Map.of("color", "warning", "variant", "filled"),
                        "advanced", Map.of("color", "error",   "variant", "filled")));
        return ApiResponse.call(HttpStatus.OK, MessageConstants.OK, config);
    }

    /** Returns hero image URL for a given page key (sql | nosql | vectordb). */
    @GetMapping("/page/{pageKey}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPageConfig(@PathVariable String pageKey) {
        return pageAssetsRepository.findByPageKey(pageKey)
                .map(p -> ApiResponse.<Map<String, String>>call(
                        HttpStatus.OK, MessageConstants.OK,
                        Map.of("heroImageUrl", p.getHeroImageUrl() != null ? p.getHeroImageUrl() : "")))
                .orElse(ApiResponse.call(HttpStatus.OK, MessageConstants.OK, Map.of("heroImageUrl", "")));
    }
}
