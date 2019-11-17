package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;

import java.util.HashMap;

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
import types.ClientRepairs;
import types.Repair;
import types.RepairList;
import utils.Logger;

public class Client extends Agent {
    private static final long serialVersionUID = 5090227891936996896L;
    private static final String subscriptionOnto = "client-station-subscription";

    private final String id;
    private AID station;

    private int repairId;
    private HashMap<Integer, Repair> repairsHistory;
    private HashMap<Integer, Repair> dayRequestRepairs;
    ClientStrategy strategy;

    public Client(String id, AID station) {
        assert id != null && station != null;
        this.id = id;
        this.station = station;
        this.repairId = 0;
        repairsHistory = new HashMap<>();     // repairs done
        dayRequestRepairs = new HashMap<>();  // repairs for the day

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

    class ClientNight extends Behaviour {
        private static final long serialVersionUID = 2838271060454701293L;

        @Override
        public void action() {
            MessageTemplate acl, onto;

            strategy.evaluateAdjustments(dayRequestRepairs);
            repairId += strategy.generateNewRepairs(dayRequestRepairs, repairId);

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
            reply.setContent(new ClientRepairs(dayRequestRepairs).make());
            send(reply);

            // Protocol C: wait for assignments...
            onto = MatchOntology("inform-client-assignment");
            acl = MatchPerformative(ACLMessage.INFORM);
            ACLMessage assign = receive(and(onto, acl));
            while (assign == null) {
                block();
                assign = receive(and(onto, acl));
            }

            RepairList list = RepairList.from(assign);

            for (int id : list.ids) {
                assert dayRequestRepairs.containsKey(id);
                Repair repair = dayRequestRepairs.remove(id);
                repairsHistory.put(id, repair);
            }
        }

        @Override
        public boolean done() {
            return false;
            // TODO ORCHESTRATION: return true on the final day.
        }
    }
}
