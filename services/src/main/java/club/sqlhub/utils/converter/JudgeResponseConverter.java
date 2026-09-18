package club.sqlhub.utils.converter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;

@SuppressWarnings("unchecked")
public class JudgeResponseConverter {

    public static SubmissionResponseDTO convertJudgeResultToSubmissionResponse(JudgeResultDTO obj) {
        SubmissionResponseDTO response = new SubmissionResponseDTO();

        // USER ID
        response.setUserId(obj.getUserId());


        if (obj.getResult() != null && obj.getResult() instanceof Map) {
            Map<String, Object> resultMap = (Map<String, Object>) obj.getResult();

            Object totalExecMs = resultMap.get("totalExecutionMs");
            if (totalExecMs != null) {
                if (totalExecMs instanceof Map) {
                    Map<String, Object> execMap = (Map<String, Object>) totalExecMs;
                    Object numberLong = execMap.get("$numberLong");
                    if (numberLong != null) {
                        response.setExecutionTime(numberLong.toString() + " ms");
                    }
                } else {
                    response.setExecutionTime(totalExecMs.toString() + " ms");
                }
            }

            Object questionId = resultMap.get("questionId");
            if (questionId != null) {
                response.setQuestionId(questionId.toString());
            }

            Object overallStatus = resultMap.get("overallStatus");
            if (overallStatus != null) {
                response.setVerdict(overallStatus.toString());
            }

            Object passedCount = resultMap.get("passedCount");
            if (passedCount != null) {
                response.setPassCount(((Number) passedCount).intValue());
            }

            Object totalCount = resultMap.get("totalCount");
            if (totalCount != null) {
                response.setTotalCount(((Number) totalCount).intValue());
            }

            Object timestamp = resultMap.get("timestamp");
            if (timestamp != null) {
                if (timestamp instanceof Map) {
                    Map<String, Object> timestampMap = (Map<String, Object>) timestamp;
                    Object dateStr = timestampMap.get("$date");
                    if (dateStr != null) {
                        response.setSubmissionTime(dateStr.toString());
                    }
                } else if (timestamp instanceof Date) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    response.setSubmissionTime(sdf.format((Date) timestamp));
                } else {
                    response.setSubmissionTime(timestamp.toString());
                }
            }

            // TEST DETAILS
            Object testDetails = resultMap.get("details");
            if (testDetails != null) {
                response.setTestDetails(testDetails);
            }
        }

        response.setType("submission");

        return response;
    }
}
