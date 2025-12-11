package club.sqlhub.mongo.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import club.sqlhub.entity.judge.SubmissionRequestDTO;
import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.mongo.repository.UserQueriesRepository;
import club.sqlhub.utils.converter.JudgeResponseConverter;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserQueriesResultService {

    private final UserQueriesRepository repo;

    public JudgeResultDTO findById(String jobId) {
        JudgeResultDTO res = repo.findByJobId(jobId);
        return res;
    }

    public List<JudgeResultDTO> findByUserId(String userId) {
        return repo.findByUserId(userId);
    }

    public Page<SubmissionResponseDTO> findByFilters(SubmissionRequestDTO req) {
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), 
    Sort.by(Sort.Direction.DESC, "result.timestamp"));
        Page<JudgeResultDTO> res = repo.findByFilters(req.getUserId(), req.getJobId(), req.getQuestionId(),
                req.getFromDate(), req.getToDate(), req.getVerdict(), req.getType(), pageable);

        return res.map(JudgeResponseConverter::convertJudgeResultToSubmissionResponse);
    }

}