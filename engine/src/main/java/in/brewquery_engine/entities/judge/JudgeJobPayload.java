package in.brewquery_engine.entities.judge;

import lombok.Data;

@Data
public class JudgeJobPayload {
    private String jobId;
    private String type;
    private String payload;
    private Long timestamp;
    private String userId;
}
