package types;

import utils.MalfunctionType;

public class ClientRequest {
    private double repairStartTime;
    private MalfunctionType malfunctionType;
    private double maxPrice;

    public ClientRequest(double repairStartTime, MalfunctionType malfunctionType, double maxPrice) {
        this.repairStartTime = repairStartTime;
        this.malfunctionType = malfunctionType;
        this.maxPrice = maxPrice;
    }

    public double getRepairStartTime() {
        return repairStartTime;
    }

    public MalfunctionType getMalfunctionType() {
        return malfunctionType;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }
}
