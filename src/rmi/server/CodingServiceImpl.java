// File: rmi/server/CodingServiceImpl.java
package rmi.server;

import rmi.model.CodingProblem;
import rmi.model.Leaderboard;
import rmi.service.CodingService;
import rmi.model.Submission;
import rmi.model.SubmissionResult;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CodingServiceImpl extends UnicastRemoteObject implements CodingService {

    private final Map<String, CodingProblem> problems = new ConcurrentHashMap<>();
    private final Map<String, List<Submission>> userSubmissions = new ConcurrentHashMap<>();
    private final Map<String, List<Submission>> problemSubmissions = new ConcurrentHashMap<>();

    public CodingServiceImpl() throws RemoteException {
        super();
        initializeSampleProblems();
    }

    private void initializeSampleProblems() {
        // Problem 1: Fibonacci
        CodingProblem fibProblem = new CodingProblem(
                "FIB01",
                "Tính số Fibonacci",
                "Viết hàm tính số Fibonacci thứ n\n\nInput: số nguyên n (0 ≤ n ≤ 20)\nOutput: số Fibonacci thứ n"
        );
        fibProblem.setDifficulty("EASY");
        fibProblem.addStarterCode("python", "def fibonacci(n):\n    # Write your code here\n    return 0");
        fibProblem.addStarterCode("javascript", "function fibonacci(n) {\n    // Write your code here\n    return 0;\n}");
        fibProblem.addTestCase("5", "5");
        fibProblem.addTestCase("0", "0");
        fibProblem.addTestCase("10", "55");
        problems.put("FIB01", fibProblem);

        // Problem 2: Factorial
        CodingProblem factProblem = new CodingProblem(
                "FACT01",
                "Tính giai thừa",
                "Viết hàm tính giai thừa của n\n\nInput: số nguyên n (0 ≤ n ≤ 10)\nOutput: n!"
        );
        factProblem.setDifficulty("EASY");
        factProblem.addStarterCode("python", "def factorial(n):\n    # Write your code here\n    return 1");
        factProblem.addStarterCode("javascript", "function factorial(n) {\n    // Write your code here\n    return 1;\n}");
        factProblem.addTestCase("5", "120");
        factProblem.addTestCase("0", "1");
        factProblem.addTestCase("4", "24");
        problems.put("FACT01", factProblem);
    }

    @Override
    public void addProblem(CodingProblem problem) throws RemoteException {
        problems.put(problem.getProblemId(), problem);
    }


    @Override
    public List<CodingProblem> getAllProblems() throws RemoteException {
        return new ArrayList<>(problems.values());
    }

    @Override
    public CodingProblem getProblem(String problemId) throws RemoteException {
        return problems.get(problemId);
    }

    @Override
    public SubmissionResult submitSolution(String problemId, String code, String language, String userId) throws RemoteException {
        CodingProblem problem = problems.get(problemId);
        if (problem == null) {
            throw new RemoteException("Problem not found: " + problemId);
        }

        String submissionId = UUID.randomUUID().toString();
        SubmissionResult result = new SubmissionResult(submissionId, problemId, userId);

        try {
            // TODO: Integrate với ScriptTask hiện có để chạy test cases
            // Tạm thời giả lập kết quả
            result.setPassedTests(3);
            result.setTotalTests(3);
            result.setPassed(true);
            result.setExecutionTime(150);
            result.setOutput("All tests passed! ✓");

        } catch (Exception e) {
            result.setPassed(false);
            result.setOutput("Error: " + e.getMessage());
        }

        // Lưu submission
        Submission submission = new Submission(submissionId, problemId, userId, code, language);
        submission.setPassed(result.isPassed());

        userSubmissions.computeIfAbsent(userId, k -> new ArrayList<>()).add(submission);
        problemSubmissions.computeIfAbsent(problemId, k -> new ArrayList<>()).add(submission);

        return result;
    }

    @Override
    public List<Submission> getUserSubmissions(String userId) throws RemoteException {
        return userSubmissions.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public Leaderboard getProblemLeaderboard(String problemId) throws RemoteException {
        // TODO: Implement leaderboard logic
        return new Leaderboard(problemId, String.valueOf(new ArrayList<>()));
    }
}