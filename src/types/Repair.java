package types;

import utils.MalfunctionType;

public class Repair {
    private MalfunctionType malfunctionType;
    private double price;

    public Repair(MalfunctionType malfunctionType, double price) {
        this.malfunctionType = malfunctionType;
        this.price = price;
    }

    public MalfunctionType getMalfunctionType() {
        return malfunctionType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double newPrice) {
        price = newPrice;
    }
}
