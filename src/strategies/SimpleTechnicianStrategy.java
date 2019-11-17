package strategies;

import jade.core.AID;
import simulation.World;
import types.Contract;

public class SimpleTechnicianStrategy extends TechnicianStrategy {
    @Override
    public Contract bestJobOffer(Contract cp1, Contract cp2) {
        // ... fuck isto vai dar trabalho
        return null;
    }

    @Override
    public boolean acceptJobOffer(Contract cp) {
        Contract last = technician.getCurrentContract();
        if (last == null) last = technician.getPreviousContract();
        Contract renewed = last.renew(cp.start, cp.end);
        return bestJobOffer(renewed, cp) == cp;
    }

    @Override
    public boolean lookForContracts() {
        int day = World.get().getDay();
        Contract current = technician.getCurrentContract();
        return current.end - day <= 3;
    }

    @Override
    public Contract renewalContract() {
        int day = World.get().getDay();
        Contract cp = technician.getCurrentContract();

        AID company = cp.company;
        String station = technician.getHomeStation().getLocalName();
        double salary = cp.salary * 1.02;
        double percentage = cp.percentage;
        int start = Math.max(cp.end + 1, day);
        int end = start + 30;
        return new Contract(company, technician.getAID(), station, salary, percentage, start, end);
    }
}
