package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;

import java.util.HashMap;

import agentbehaviours.AwaitDayBehaviour;
import agentbehaviours.AwaitNightBehaviour;
import agentbehaviours.SequentialLoopBehaviour;
import agentbehaviours.SubscribeBehaviour;
import agentbehaviours.WaitingBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
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
        Logger.client(id, "Setup " + id);

        String clientSub = World.get().getClientStationService();

        // Setup
        addBehaviour(new SubscribeBehaviour(this, station, clientSub));
        addBehaviour(new GenerateNewRepairs(this));

        SequentialLoopBehaviour loop = new SequentialLoopBehaviour(this);
        // Night
        loop.addSubBehaviour(new AwaitNightBehaviour(this));
        loop.addSubBehaviour(new AnswerStationPrompt(this));
        loop.addSubBehaviour(new ReceiveRepairUpdates(this));
        // Day
        loop.addSubBehaviour(new AwaitDayBehaviour(this));
        loop.addSubBehaviour(new GenerateNewRepairs(this));
        addBehaviour(loop);
    }

    @Override
    protected void takeDown() {
        Logger.client(id, "Client Terminated!");
    }

    private class GenerateNewRepairs extends OneShotBehaviour {
        private static final long serialVersionUID = 4988514485354327443L;

        GenerateNewRepairs(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            strategy.evaluateAdjustments(dayRequestRepairs);
            repairId += strategy.generateNewJobs(dayRequestRepairs, repairId);
            Logger.client(id, "Generated new jobs and price adjustments");
        }
    }

    private class AnswerStationPrompt extends WaitingBehaviour {
        private static final long serialVersionUID = -9102240605267260487L;

        AnswerStationPrompt(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            MessageTemplate onto = MatchOntology(World.get().getPromptClient());
            MessageTemplate acl = MatchPerformative(ACLMessage.REQUEST);

            Logger.client(id, "Waiting request from station");

            // Protocol A: wait for request message
            ACLMessage request = receive(and(onto, acl));
            if (request == null) {
                block();
                return;
            }

            // Protocol B: answer message
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(new ClientRepairs(dayRequestRepairs).make());
            send(reply);

            Logger.client(id, "Sent new jobs to station");

            finalize();
        }
    }

    private class ReceiveRepairUpdates extends WaitingBehaviour {
        private static final long serialVersionUID = -4937376307364576863L;

        ReceiveRepairUpdates(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            // Protocol C: wait for assignments...
            MessageTemplate onto = MatchOntology(World.get().getInformClient());
            MessageTemplate acl = MatchPerformative(ACLMessage.INFORM);
            ACLMessage assign = receive(and(onto, acl));
            if (assign == null) {
                block();
                return;
            }

            RepairList list = RepairList.from(assign);

            for (int id : list.ids) {
                // assert dayRequestRepairs.containsKey(id);
                Repair repair = dayRequestRepairs.remove(id);
                repairsHistory.putIfAbsent(id, repair);
            }

            Logger.client(id, "Received repair updates from station (" + list.ids.size() + ")");

            finalize();
        }
    }
}
