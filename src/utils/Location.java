package utils;

import java.io.Serializable;

public class Location implements Serializable {

    private final double x;
    private final double y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
