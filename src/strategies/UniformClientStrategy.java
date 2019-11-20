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
            double d = rand.nextDouble(), price = repair.getPrice();
            switch (repair.getMalfunctionType()) {
            case EASY:
                repair.setPrice(price + 2.5 * d);
                break;
            case MEDIUM:
                repair.setPrice(price + 8 * d);
                break;
            case HARD:
                repair.setPrice(price + 28 * d);
                break;
            }
        }
    }

    @Override
    public int generateNewJobs(Map<Integer, Repair> dayRequestRepairs, int repairId) {
        Random rand = ThreadLocalRandom.current();
        int numberRequests = rand.nextInt(11);
        for (int i = 0; i < numberRequests; i++) {
            double d = rand.nextDouble();
            int t = rand.nextInt(14);
            MalfunctionType type;
            double price;

            if (t <= 0) {
                type = MalfunctionType.HARD;
                price = 320 + 20 * d;
            } else if (t <= 3) {
                type = MalfunctionType.MEDIUM;
                price = 90 + 7 * d;
            } else {
                type = MalfunctionType.EASY;
                price = 30 + 2 * d;
            }

            Repair request = new Repair(type, price);
            dayRequestRepairs.put(repairId, request);
            repairId++;
        }
        return numberRequests;
    }
}
