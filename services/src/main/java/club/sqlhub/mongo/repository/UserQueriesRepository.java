package club.sqlhub.mongo.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;

@Repository
public interface UserQueriesRepository extends MongoRepository<JudgeResultDTO, String> {

        public JudgeResultDTO findByJobId(String jobId);

        public List<JudgeResultDTO> findByUserId(String userId);

        @Query("{ " +
                        "  'userId': ?0, " +
                        "  $and: [ " +
                        "    { $or: [ " +
                        "      { $expr: { $eq: [?1, null] } }, " +
                        "      { $expr: { $eq: [?1, ''] } }, " +
                        "      { 'result.jobId': ?1 } " +
                        "    ] }, " +
                        "    { $or: [ " +
                        "      { $expr: { $eq: [?2, null] } }, " +
                        "      { $expr: { $eq: [?2, ''] } }, " +
                        "      { 'result.questionId': ?2 } " +
                        "    ] }, " +
                        "    { $or: [ " +
                        "      { $expr: { $eq: [?3, null] } }, " +
                        "      { 'result.timestamp': { $gte: ?3 } } " +
                        "    ] }, " +
                        "    { $or: [ " +
                        "      { $expr: { $eq: [?4, null] } }, " +
                        "      { 'result.timestamp': { $lte: ?4 } } " +
                        "    ] }, " +
                        "    { $or: [ " +
                        "      { $expr: { $eq: [?5, null] } }, " +
                        "      { $expr: { $eq: [?5, ''] } }, " +
                        "      { 'result.overallStatus': ?5 } " +
                        "    ] } " +
                        "  ] " +
                        "}")
        Page<JudgeResultDTO> findByFilters(
                        String userId,
                        String jobId,
                        String questionId,
                        Date fromDate,
                        Date toDate,
                        String verdict,
                        String type,
                        Pageable pageable);
}
