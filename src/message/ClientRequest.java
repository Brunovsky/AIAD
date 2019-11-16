package message;

import utils.MalfunctionType;

public class ClientRequest {
    private double repairTime;
    private MalfunctionType malfunctionType;
    private double maxPrice;

    public ClientRequest(double repairTime, MalfunctionType malfunctionType, double maxPrice) {
        this.repairTime = repairTime;
        this.malfunctionType = malfunctionType;
        this.maxPrice = maxPrice;
    }

    public double getRepairTime() {
        return repairTime;
    }

    public MalfunctionType getMalfunctionType() {
        return malfunctionType;
    }

    public double getMaxPrice() {
        return maxPrice;
    }
}
