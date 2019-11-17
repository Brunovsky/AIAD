package types;

import utils.MalfunctionType;

public class Job {
    public final MalfunctionType type;
    public final double price;

    public Job(MalfunctionType type, double price) {
        this.type = type;
        this.price = price;
    }
}
