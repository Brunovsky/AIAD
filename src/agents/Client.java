package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;

import java.util.HashMap;

import agentbehaviours.AwaitDay;
import agentbehaviours.AwaitNight;
import agentbehaviours.SubscribeBehaviour;
import agentbehaviours.WorldLoop;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.World;
import strategies.ClientStrategy;
import types.ClientRepairs;
import types.Repair;
import types.RepairList;
import utils.Logger;

public class Client extends Agent {
    private static final long serialVersionUID = 5090227891936996896L;

    private final String id;
    private AID station;
    private int repairId;

    private HashMap<Integer, Repair> repairsHistory;
    private HashMap<Integer, Repair> dayRequestRepairs;

    private final ClientStrategy strategy;

    public Client(String id, ClientStrategy strategy, AID station) {
        assert id != null && strategy != null && station != null;
        this.id = id;
        this.station = station;
        this.repairId = 0;

        this.repairsHistory = new HashMap<>();     // repairs done
        this.dayRequestRepairs = new HashMap<>();  // repairs for the day

        this.strategy = strategy;
        strategy.setClient(this);
    }

    @Override
    protected void setup() {
        Logger.info(id, "Setup " + id);

        String clientSub = World.get().getClientStationService();

        // SETUP
        addBehaviour(new SubscribeBehaviour(this, station, clientSub));
        addBehaviour(new GenerateNewRepairs(this));

        SequentialBehaviour sequential = new SequentialBehaviour(this);

        // NIGHT
        sequential.addSubBehaviour(new AwaitNight(this));
        sequential.addSubBehaviour(new ClientNight(this));

        // DAY
        sequential.addSubBehaviour(new AwaitDay(this));
        sequential.addSubBehaviour(new GenerateNewRepairs(this));

        addBehaviour(new WorldLoop(sequential));
    }

    @Override
    protected void takeDown() {
        Logger.warn(id, "Client Terminated!");
    }

    class GenerateNewRepairs extends OneShotBehaviour {
        private static final long serialVersionUID = 4988514485354327443L;

        GenerateNewRepairs(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            strategy.evaluateAdjustments(dayRequestRepairs);
            repairId += strategy.generateNewRepairs(dayRequestRepairs, repairId);
        }
    }

    class ClientNight extends OneShotBehaviour {
        private static final long serialVersionUID = 2838271060454701293L;

        ClientNight(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate acl, onto;

            // Protocol A: wait for request message
            onto = MatchOntology(World.get().getPromptClient());
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
            onto = MatchOntology(World.get().getInformClient());
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
    }
}
