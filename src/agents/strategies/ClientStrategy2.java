package agents.strategies;

import types.ClientRequest;

import java.util.HashMap;

public class ClientStrategy2 implements ClientStrategy {
    @Override
    public HashMap<Integer, ClientRequest> generateNewRepairs(int repairId) {
        HashMap<Integer, ClientRequest> newRepairs = new HashMap<>();

        // TODO implement

        return newRepairs;
    }

    @Override
    public HashMap<Integer, Double> evaluateAdjustments(HashMap<Integer, ClientRequest> adjustments) {
        HashMap<Integer, Double> needsAdjustments = new HashMap<>();

        // TODO implement

        return needsAdjustments;
    }
}
