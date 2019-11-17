package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;
import static message.Messages.getClientRequestMessage;
import static message.Messages.parseClientResponseMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import agentbehaviours.SubscribeBehaviour;
import agentbehaviours.UnsubscribeBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import strategies.ClientStrategy;
import strategies.ClientStrategy1;
import types.Repair;
import utils.Logger;

public class Client extends Agent {
    private static final long serialVersionUID = 5090227891936996896L;
    private static final String subscriptionOnto = "client-station-subscription";

    private final String id;
    private AID station;

    private int repairId;
    private HashMap<Integer, Repair> repairsHistory;
    private HashMap<Integer, Repair> dayRequestRepairs;
    private HashMap<Integer, Repair> requestAdjustments;
    ClientStrategy strategy;

    public Client(String id, AID station) {
        assert id != null && station != null;
        this.id = id;
        this.station = station;
        this.repairId = 0;
        repairsHistory = new HashMap<>();      // repairs done
        dayRequestRepairs = new HashMap<>();   // repairs for the day
        requestAdjustments = new HashMap<>();  // repairs that need an adjustments

        // TODO Choose strategy, maybe send as a parameter in Client contructor
        strategy = new ClientStrategy1();
    }

    @Override
    protected void setup() {
        Logger.info(getLocalName(), "Setup " + id);

        SequentialBehaviour sequential = new SequentialBehaviour(this);
        sequential.addSubBehaviour(new SubscribeBehaviour(this, station, subscriptionOnto));
        sequential.addSubBehaviour(new ClientNight());
        sequential.addSubBehaviour(new UnsubscribeBehaviour(this, station, subscriptionOnto));

        addBehaviour(sequential);
    }

    @Override
    protected void takeDown() {
        Logger.warn(getLocalName(), "Client Terminated!");
    }

    private HashMap<Integer, Repair> generateNewRepairs() {
        HashMap<Integer, Repair> newRepairs = strategy.generateNewRepairs(repairId);
        repairId += newRepairs.size();
        return newRepairs;
    }

    private HashMap<Integer, Double> evaluateAdjustments() {
        return strategy.evaluateAdjustments(requestAdjustments);
    }

    class ClientNight extends Behaviour {
        private static final long serialVersionUID = 2838271060454701293L;

        @Override
        public void action() {
            MessageTemplate acl, onto;

            HashMap<Integer, Repair> repairs = generateNewRepairs();
            HashMap<Integer, Double> adjustments = evaluateAdjustments();

            // Protocol A: wait for request message
            onto = MatchOntology("prompt-client-malfunctions");
            acl = MatchPerformative(ACLMessage.REQUEST);
            ACLMessage request = receive(and(onto, acl));
            while (request == null) {
                block();
                request = receive(and(onto, acl));
            }

            // Protocol B: answer message
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(getClientRequestMessage(repairs, adjustments));
            send(reply);

            // Protocol C: wait for assignments...
            onto = MatchOntology("inform-client-assignment");
            acl = MatchPerformative(ACLMessage.INFORM);
            ACLMessage assign = receive(and(onto, acl));
            while (assign == null) {
                block();
                assign = receive(and(onto, acl));
            }

            HashMap<Integer, Repair> assignedRepairs = parseClientResponseMessage(
                assign.getContent());

            // adding all repairs who have a technician assigned to history
            repairsHistory.putAll(assignedRepairs);

            Iterator it = assignedRepairs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                // remove repairs already assigned by a technician from all day repairs requests
                dayRequestRepairs.remove(pair.getKey());
            }

            // move remaining repairs from day repairs to need adjustments list
            if (dayRequestRepairs.size() != 0) {
                requestAdjustments.putAll(dayRequestRepairs);
            }

            dayRequestRepairs = new HashMap<>();
        }

        @Override
        public boolean done() {
            return false;
            // return true on the final day.
        }
    }
}
