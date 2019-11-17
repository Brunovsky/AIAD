package strategies;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import types.Repair;
import utils.MalfunctionType;

public class ClientStrategy1 implements ClientStrategy {
    @Override
    public HashMap<Integer, Repair> generateNewRepairs(int repairId) {
        HashMap<Integer, Repair> newRepairs = new HashMap<>();
        Random rand = new Random();
        // TODO CONSTANTS
        int numberRequests = rand.nextInt(11);  // generating a max of 10 repairs requests
        for (int i = 0; i < numberRequests; i++) {
            // max price between 20 and 80
            double maxPrice = 20 + (80 - 20) * rand.nextDouble();
            MalfunctionType type = MalfunctionType.make(rand.nextInt(3));
            Repair request = new Repair(type, maxPrice);
            newRepairs.put(repairId, request);
            repairId++;
        }
        return newRepairs;
    }

    @Override
    public HashMap<Integer, Double> evaluateAdjustments(HashMap<Integer, Repair> adjustments) {
        HashMap<Integer, Double> needsAdjustments = new HashMap<>();
        Random rand = new Random();
        // TODO CONSTANTS
        Iterator it = adjustments.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            double nextPrice = 20 * rand.nextDouble() + ((Repair) pair.getValue()).getPrice();
            needsAdjustments.put((Integer) pair.getKey(), nextPrice);
        }
        return needsAdjustments;
    }
}
