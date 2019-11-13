package message;

import utils.Location;
import utils.MalfunctionType;

import java.io.*;

public class ClientMessage implements Serializable {

    // Message from Client to Technician
    private Location location;
    private MalfunctionType type;
    private double requestSendTime;

    public ClientMessage(Location location, MalfunctionType type, double requestSendTime) {
        this.location = location;
        this.type = type;
        this.requestSendTime = requestSendTime;
    }

    public Location getLocation() {
        return location;
    }

    public MalfunctionType getType() {
        return type;
    }

    public double getRequestSendTime() {
        return requestSendTime;
    }
}
