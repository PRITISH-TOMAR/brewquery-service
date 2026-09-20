package in.brewquery_engine.entities.judge;

import lombok.Data;

@Data
public class SubmissionStatusResponseDTO {
    private String jobId;
    private String status;
    private String message;
}
