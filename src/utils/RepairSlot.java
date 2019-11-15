package utils;

import simulation.World;

public class RepairSlot {
    private MalfunctionType type;
    private double duration;  // slot duration
    private double startSlotTime;
    private double startRepairTime;
    private double endRepairTime;
    private double endSlotTime;
    private Location clientLocation;
    private double repairPrice;
    private String clientId;

    public RepairSlot(MalfunctionType type, double startSlotTime, Location clientLocation,
                      double repairPrice, String clientId, Location technicianLocation) {
        double distance = Location.distance(clientLocation, technicianLocation);
        double malfunctionDuration = World.get().malfunctionDuration(type);

        this.type = type;
        this.startSlotTime = startSlotTime;
        this.clientLocation = clientLocation;
        this.repairPrice = repairPrice;
        this.clientId = clientId;
        this.duration = World.get().workDuration(distance, type);
        this.endSlotTime = startSlotTime + this.duration;
        this.startRepairTime = startSlotTime + World.get().travelDuration(distance);
        this.endRepairTime = this.startRepairTime + malfunctionDuration;
    }

    public MalfunctionType getType() {
        return type;
    }

    public double getDuration() {
        return duration;
    }

    public double getStartSlotTime() {
        return startSlotTime;
    }

    public double getStartRepairTime() {
        return startRepairTime;
    }

    public double getEndRepairTime() {
        return endRepairTime;
    }

    public double getEndSlotTime() {
        return endSlotTime;
    }

    public Location getClientLocation() {
        return clientLocation;
    }

    public double getRepairPrice() {
        return repairPrice;
    }

    public String getClientId() {
        return clientId;
    }
}
