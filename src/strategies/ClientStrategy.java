package strategies;

import java.util.Map;

import types.Repair;

public interface ClientStrategy {
    public void evaluateAdjustments(Map<Integer, Repair> dayRequestRepairs);

    public int generateNewRepairs(Map<Integer, Repair> dayRequestRepairs, int repairId);
}
