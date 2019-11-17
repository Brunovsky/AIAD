package message;

import java.io.Serializable;

public class TechnicianMessage implements Serializable {

    // Messages response from Technician to Client
    private double repairPrice;
    private double startRepairTime;

    public TechnicianMessage(double repairPrice, double startRepairTime) {
        this.repairPrice = repairPrice;
        this.startRepairTime = startRepairTime;
    }

    public double getRepairPrice() {
        return repairPrice;
    }

    public double getStartRepairTime() {
        return startRepairTime;
    }
}
