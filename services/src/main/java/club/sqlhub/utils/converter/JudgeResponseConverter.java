package club.sqlhub.utils.converter;

import java.text.SimpleDateFormat;
import java.util.Map;

import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;

@SuppressWarnings("unchecked")
public class JudgeResponseConverter {

    public static SubmissionResponseDTO convertJudgeResultToSubmissionResponse(JudgeResultDTO obj) {
        SubmissionResponseDTO response = new SubmissionResponseDTO();

        response.setId(obj.getJobId());
        response.setUserId(obj.getUserId());
        response.setQuestionId(obj.getQuestionId());
        response.setQuestionTitle(obj.getQuestionTitle());
        response.setDataset(obj.getDataset());
        response.setLevel(obj.getLevel());
        response.setLanguage(obj.getLanguage());

        if (obj.getSubmittedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            response.setSubmittedAt(sdf.format(obj.getSubmittedAt()));
        }

        if (obj.getResult() != null && obj.getResult() instanceof Map) {
            Map<String, Object> resultMap = (Map<String, Object>) obj.getResult();

            Object totalExecMs = resultMap.get("totalExecutionMs");
            if (totalExecMs != null) {
                response.setTimeTaken(totalExecMs.toString() + " ms");
            }

            Object overallStatus = resultMap.get("overallStatus");
            if (overallStatus != null) {
                response.setResult(overallStatus.toString());
            }

            Object passedCount = resultMap.get("passedCount");
            if (passedCount != null) {
                response.setPassCount(((Number) passedCount).intValue());
            }

            Object totalCount = resultMap.get("totalCount");
            if (totalCount != null) {
                response.setTotalCount(((Number) totalCount).intValue());
            }

            Object testDetails = resultMap.get("testDetails");
            if (testDetails != null) {
                response.setTestDetails(testDetails);
            }
        }

        return response;
    }
}
