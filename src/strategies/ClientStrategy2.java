package strategies;

import java.util.HashMap;

import types.Repair;

public class ClientStrategy2 implements ClientStrategy {
    @Override
    public HashMap<Integer, Repair> generateNewRepairs(int repairId) {
        HashMap<Integer, Repair> newRepairs = new HashMap<>();

        // TODO STRATEGY implement

        return newRepairs;
    }

    @Override
    public HashMap<Integer, Double> evaluateAdjustments(HashMap<Integer, Repair> adjustments) {
        HashMap<Integer, Double> needsAdjustments = new HashMap<>();

        // TODO STRATEGY implement

        return needsAdjustments;
    }
}
