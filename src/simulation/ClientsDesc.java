package simulation;

import utils.ClientType;

public class ClientsDesc {
    public int number;
    public ClientType personality;

    public ClientsDesc(int number, ClientType personality) {
        assert personality != null;
        this.number = number;
        this.personality = personality;
    }
}
