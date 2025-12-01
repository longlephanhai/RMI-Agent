package rmi.service;
import rmi.model.CodingProblem;
import rmi.model.Leaderboard;
import rmi.model.Submission;
import rmi.model.SubmissionResult;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
public interface CodingService extends Remote {
    // Problem management
    void addProblem(CodingProblem problem) throws RemoteException;
    List<CodingProblem> getAllProblems() throws RemoteException;
    CodingProblem getProblem(String problemId) throws RemoteException;

    // Submission & evaluation
    SubmissionResult submitSolution(String problemId, String code, String language, String userId) throws RemoteException;
    List<Submission> getUserSubmissions(String userId) throws RemoteException;
    Leaderboard getProblemLeaderboard(String problemId) throws RemoteException;
}