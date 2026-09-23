package club.sqlhub.mongo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import club.sqlhub.Repository.JudgeResultSQLRepository;
import club.sqlhub.entity.judge.JudgeServerJobDTO.SubmissionResponseDTO;
import club.sqlhub.entity.judge.SubmissionRequestDTO;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.utils.converter.JudgeResponseConverter;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserQueriesResultService {

    private final JudgeResultSQLRepository repo;

    public JudgeResultDTO findById(String jobId) {
        return repo.findByJobId(jobId);
    }

    public JudgeResultDTO save(JudgeResultDTO result) {
        return repo.save(result);
    }

    public List<JudgeResultDTO> findByUserId(String userId) {
        return repo.findByUserId(userId);
    }

    public Page<SubmissionResponseDTO> findByFilters(SubmissionRequestDTO req) {
        int limit  = req.getSize();
        int offset = req.getPage() * req.getSize();

        List<JudgeResultDTO> rows = repo.findByFilters(
                req.getUserId(), req.getJobId(), req.getQuestionId(),
                req.getFromDate(), req.getToDate(), req.getVerdict(), limit, offset);

        long total = repo.countByFilters(
                req.getUserId(), req.getJobId(), req.getQuestionId(),
                req.getFromDate(), req.getToDate(), req.getVerdict());

        List<SubmissionResponseDTO> content = rows.stream()
                .map(JudgeResponseConverter::convertJudgeResultToSubmissionResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, PageRequest.of(req.getPage(), req.getSize()), total);
    }
}
