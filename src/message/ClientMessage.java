package message;

import utils.ClientType;
import utils.Location;

import java.io.*;

public class ClientMessage implements Serializable {

    // Message from Client to Technician
    private Location location;
    private ClientType clientType;

    // TODO: Add more attributes to message

    public ClientMessage(Location location, ClientType type) {
        this.location = location;
        this.clientType = type;
    }

    public Location getLocation() {
        return location;
    }

    public ClientType getClientType() {
        return clientType;
    }
}
