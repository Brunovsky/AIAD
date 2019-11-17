package types;

import jade.core.AID;

public class RepairKey {
    public final AID client;
    public final int id;

    public RepairKey(AID client, int id) {
        this.client = client;
        this.id = id;
    }
}
