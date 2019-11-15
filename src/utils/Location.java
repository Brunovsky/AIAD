package utils;

import java.io.Serializable;

public class Location implements Serializable {
    private final double x;
    private final double y;

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public static double distance(Location a, Location b) {
        double x = a.getX() - b.getX(), y = a.getY() - b.getY();
        return Math.sqrt(x * x + y * y);
    }
}
