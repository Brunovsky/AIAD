package strategies;

import java.util.HashMap;

import types.Repair;

public interface ClientStrategy {
    public HashMap<Integer, Repair> generateNewRepairs(int repairId);

    public HashMap<Integer, Double> evaluateAdjustments(HashMap<Integer, Repair> adjustments);
}
