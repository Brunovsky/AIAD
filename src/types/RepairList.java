package types;

import java.util.HashSet;
import java.util.Set;

import jade.lang.acl.ACLMessage;

public class RepairList {
    public final Set<Integer> ids;

    public RepairList() {
        this.ids = new HashSet<>();
    }

    public RepairList(Set<Integer> ids) {
        this.ids = ids;
    }

    public String make() {
        StringBuilder builder = new StringBuilder();
        for (int id : ids) {
            if (builder.length() > 0) builder.append(":");
            builder.append(id);
        }
        return builder.toString();
    }

    public static RepairList from(ACLMessage message) {
        String[] parts = message.getContent().split(":");
        RepairList list = new RepairList();
        for (String part : parts) list.ids.add(Integer.parseInt(part));
        return list;
    }
}
