package message;

import utils.Location;

import java.io.*;

public class ClientMessage implements Serializable {

    // Message from Client to Technician
    private Location location;

    // TODO: Add more attributes to message

    public ClientMessage(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
