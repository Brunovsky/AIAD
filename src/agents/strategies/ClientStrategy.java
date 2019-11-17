package agents.strategies;

import types.ClientRequest;

import java.util.HashMap;

public interface ClientStrategy {

    public HashMap<Integer, ClientRequest> generateNewRepairs(int repairId);

    public HashMap<Integer, Double> evaluateAdjustments(HashMap<Integer, ClientRequest> adjustments);
}
