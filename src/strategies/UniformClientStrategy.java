package strategies;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import types.Repair;
import utils.MalfunctionType;

public class UniformClientStrategy extends ClientStrategy {
    @Override
    public void evaluateAdjustments(Map<Integer, Repair> dayRequestRepairs) {
        Random rand = ThreadLocalRandom.current();
        for (int id : dayRequestRepairs.keySet()) {
            Repair repair = dayRequestRepairs.get(id);
            double nextPrice = 20 * rand.nextDouble() + repair.getPrice();  // TODO CONSTANTS
            repair.setPrice(nextPrice);
        }
    }

    @Override
    public int generateNewJobs(Map<Integer, Repair> dayRequestRepairs, int repairId) {
        Random rand = ThreadLocalRandom.current();
        int numberRequests = rand.nextInt(6);  // TODO CONSTANTS
        for (int i = 0; i < numberRequests; i++) {
            // TODO CONSTANTS: max price between 20 and 80
            double maxPrice = 20 + (80 - 20) * rand.nextDouble();
            MalfunctionType type = MalfunctionType.make(rand.nextInt(3));
            Repair request = new Repair(type, maxPrice);
            dayRequestRepairs.put(repairId, request);
            repairId++;
        }
        return numberRequests;
    }
}
