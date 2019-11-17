package types;

import java.util.ArrayList;

import jade.core.AID;
import simulation.World;

public class TechnicianHistory {
    public final AID technician;
    public final ArrayList<Contract> contracts;
    public final WorkFinance total;

    public TechnicianHistory(AID technician) {
        this.technician = technician;
        this.contracts = new ArrayList<>();
        this.total = new WorkFinance();
    }

    public Contract currentContract() {
        if (contracts.isEmpty()) return null;
        Contract last = contracts.get(contracts.size() - 1);
        int day = World.get().getDay();
        if (last.start <= day && day <= last.end) {
            return last;
        } else {
            return null;
        }
    }

    public void addContract(Contract contract) {
        contracts.add(contract);
    }

    public void add(WorkFinance finance) {
        total.add(finance);
        currentContract().history.add(finance);
    }
}
