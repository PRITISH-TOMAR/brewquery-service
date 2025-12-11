package club.sqlhub.entity.judge;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Date;

@Data
public class SubmissionRequestDTO {

    @NotBlank(message = "userId is required")
    @Size(max = 100, message = "userId cannot exceed 100 characters")
    private String userId;

    @Size(max = 100, message = "jobId cannot exceed 100 characters")
    private String jobId;

    @Size(max = 100, message = "questionId cannot exceed 100 characters")
    private String questionId;

    private Date fromDate;

    private Date toDate;

    @Size(max = 50, message = "verdict cannot exceed 50 characters")
    private String verdict;

    @Size(max = 50, message = "type cannot exceed 50 characters")
    private String type;

    // Pagination parameters
    @Min(value = 0, message = "page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "size must be >= 1")
    @Max(value = 100, message = "size cannot exceed 100")
    private int size = 10;


    @AssertTrue(message = "fromDate cannot be after toDate")
    public boolean isValidDateRange() {
        if (fromDate != null && toDate != null) {
            return !fromDate.after(toDate);
        }
        return true;
    }
}