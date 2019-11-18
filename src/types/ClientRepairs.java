package types;

import java.util.HashMap;
import java.util.Map;

import jade.lang.acl.ACLMessage;
import utils.MalfunctionType;

public class ClientRepairs {
    public final Map<Integer, Repair> list;

    public ClientRepairs() {
        this.list = new HashMap<>();
    }

    public ClientRepairs(Map<Integer, Repair> list) {
        this.list = list;
    }

    public String make() {
        StringBuilder builder = new StringBuilder();
        for (int id : list.keySet()) {
            Repair repair = list.get(id);
            int value = repair.getMalfunctionType().getValue();
            if (builder.length() > 0) builder.append(';');
            builder.append(String.format("%d=%d=%f", id, value, repair.getPrice()));
        }
        return builder.toString();
    }

    public static ClientRepairs from(ACLMessage message) {
        String[] parts = message.getContent().split(";|=");
        ClientRepairs repairs = new ClientRepairs();
        for (int i = 0; i + 2 < parts.length;) {
            int id = Integer.parseInt(parts[i++]);
            MalfunctionType type = MalfunctionType.make(Integer.parseInt(parts[i++]));
            double price = Double.parseDouble(parts[i++]);
            repairs.list.put(id, new Repair(type, price));
        }
        return repairs;
    }
}
