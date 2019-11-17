package types;

import agents.Technician;
import jade.core.AID;
import utils.MalfunctionType;

public class Repair {
    private double startTime;
    private MalfunctionType malfunctionType;
    private double duration;
    private double price;
    private String assignedTechnician; //technician name or AID.toString()

    public Repair(double startTime, MalfunctionType malfunctionType, double duration, double price, String assignedTechnician) {
        this.startTime = startTime;
        this.malfunctionType = malfunctionType;
        this.duration = duration;
        this.price = price;
        this.assignedTechnician = assignedTechnician;
    }

    public double getStartTime() {
        return startTime;
    }

    public MalfunctionType getMalfunctionType() {
        return malfunctionType;
    }

    public double getDuration() {
        return duration;
    }

    public double getPrice() {
        return price;
    }

    public String getAssignedTechnician() {
        return assignedTechnician;
    }
}
