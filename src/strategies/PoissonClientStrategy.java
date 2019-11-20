package strategies;

import java.util.Map;

import types.Repair;

// Not implemented
public class PoissonClientStrategy extends ClientStrategy {
    @Override
    public void evaluateAdjustments(Map<Integer, Repair> dayRequestRepairs) {}

    @Override
    public int generateNewJobs(Map<Integer, Repair> dayRequestRepairs, int repairId) {
        return 0;
    }
}
