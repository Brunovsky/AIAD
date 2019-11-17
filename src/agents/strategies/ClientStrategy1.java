package agents.strategies;

import types.ClientRequest;
import utils.MalfunctionType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class ClientStrategy1 implements ClientStrategy {
    @Override
    public HashMap<Integer, ClientRequest> generateNewRepairs(int repairId) {
        HashMap<Integer, ClientRequest> newRepairs = new HashMap<>();
        Random rand = new Random();
        int numberRequests = rand.nextInt(11); //generating a max of 10 repairs requests
        double repairStartTime = 0;
        for(int i = 0; i < numberRequests; i++){
            repairStartTime = repairStartTime + ((1440 - repairStartTime) / (numberRequests - i) - repairStartTime) * rand.nextDouble();
            // max price between 20 and 80
            double maxPrice = 20 + (80 - 20) * rand.nextDouble();
            MalfunctionType type = MalfunctionType.make(rand.nextInt(3));
            ClientRequest request = new ClientRequest(repairStartTime, type, maxPrice);
            newRepairs.put(repairId, request);
            repairId++;
        }
        return newRepairs;
    }

    @Override
    public HashMap<Integer, Double> evaluateAdjustments(HashMap<Integer, ClientRequest> adjustments) {
        HashMap<Integer, Double> needsAdjustments = new HashMap<>();
        Random rand = new Random();
        Iterator it = adjustments.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            double nextPrice = 20 * rand.nextDouble() + ((ClientRequest) pair.getValue()).getMaxPrice();
            needsAdjustments.put((Integer) pair.getKey(), nextPrice);
        }
        return needsAdjustments;
    }
}
