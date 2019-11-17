package types;

import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class Proposal {
    public final AID company;
    public final Map<Integer, Double> offers;

    public Proposal(AID company) {
        this.company = company;
        this.offers = new HashMap<>();
    }

    public String make() {
        StringBuilder builder = new StringBuilder();
        for (Integer id : offers.keySet()) {
            if (builder.length() > 0) builder.append(';');
            builder.append(id + ":" + offers.get(id));
        }
        return builder.toString();
    }

    public static Proposal from(ACLMessage message) {
        String[] offers = message.getContent().split(";");
        Proposal proposal = new Proposal(message.getSender());
        for (String offer : offers) {
            String[] part = offer.split(":");
            assert part.length == 2;
            proposal.offers.put(Integer.parseInt(part[0]), Double.parseDouble(part[1]));
        }
        return proposal;
    }
}
