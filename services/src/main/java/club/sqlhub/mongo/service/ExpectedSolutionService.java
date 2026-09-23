package club.sqlhub.mongo.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;

import club.sqlhub.Repository.ExpectedSolutionSQLRepository;
import club.sqlhub.mongo.models.ExpectedSolution;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpectedSolutionService {

    private final ExpectedSolutionSQLRepository repo;

    public ExpectedSolution getOne(String questionId) {
        return repo.findByQuestionId(questionId).stream().findFirst().orElse(null);
    }

    public ExpectedSolution save(ExpectedSolution sol) {
        return repo.save(sol);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }

    public ExpectedSolution getExact(String id) {
        return repo.findById(id);
    }

    public ExpectedSolution addSolution(String questionId, String sqlMode, ExpectedSolution.SolutionEntry newEntry) {
        ExpectedSolution sol = getOne(questionId);

        if (sol == null) {
            sol = new ExpectedSolution();
            sol.setId(UUID.randomUUID().toString());
            sol.setQuestionId(questionId);
            sol.setSqlMode(sqlMode);
            sol.setSolutions(new ArrayList<>());
        }

        sol.getSolutions().add(newEntry);
        return repo.save(sol);
    }

    public ExpectedSolution updateSolutionEntry(String questionId, String sqlMode, int index, ExpectedSolution.SolutionEntry updated) {
        ExpectedSolution sol = getOne(questionId);
        if (sol == null) return null;

        if (index < 0 || index >= sol.getSolutions().size()) return null;

        sol.getSolutions().set(index, updated);
        return repo.save(sol);
    }

    public ExpectedSolution.SolutionEntry findMatchingSolution(ExpectedSolution sol, String actualHash) {
        if (sol == null || sol.getSolutions() == null) return null;

        for (ExpectedSolution.SolutionEntry entry : sol.getSolutions()) {
            if (actualHash.equals(entry.getResultHash())) {
                return entry;
            }
        }
        return null;
    }
}
