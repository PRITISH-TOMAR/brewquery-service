package club.sqlhub.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import club.sqlhub.service.AdminAssetService;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminAssetService adminAssetService;

    /** Upload / replace the cover image for a dataset. */
    @PostMapping(value = "/dataset/{datasetId}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadDatasetCover(
            @PathVariable String datasetId,
            @RequestParam("file") MultipartFile file) {
        return adminAssetService.uploadDatasetCover(datasetId, file);
    }

    /** Upload / replace the ER diagram for a dataset. */
    @PostMapping(value = "/dataset/{datasetId}/er", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadDatasetEr(
            @PathVariable String datasetId,
            @RequestParam("file") MultipartFile file) {
        return adminAssetService.uploadDatasetEr(datasetId, file);
    }

    /** Upload / replace the hero image for a page (pageKey: sql | nosql | vectordb). */
    @PostMapping(value = "/page/{pageKey}/hero", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadPageHero(
            @PathVariable String pageKey,
            @RequestParam("file") MultipartFile file) {
        return adminAssetService.uploadPageHero(pageKey, file);
    }
}
