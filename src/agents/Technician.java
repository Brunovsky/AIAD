package agents;

import static agents.Technician.State.UNEMPLOYED;
import static agents.Technician.State.WORKING;
import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import agents.strategies.TechnicianStrategy;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.World;
import types.Contract;
import types.WorkLog;
import utils.Logger;

public class Technician extends Agent {
    private static final long serialVersionUID = 2763283727137639385L;

    private final String id;
    private final AID homeStation;
    private AID station, company;

    private final TechnicianStrategy strategy;

    private final Map<Integer, WorkLog> workHistory;
    private final ArrayList<Contract> contractHistory;
    private Contract currentContract, nextContract;
    private State state;

    public enum State { WORKING, MOVING, UNEMPLOYED }

    public Technician(String id, AID homeStation, AID company, TechnicianStrategy strategy) {
        assert id != null && homeStation != null && company != null && strategy != null;
        this.id = id;
        this.homeStation = homeStation;
        this.station = homeStation;
        this.company = company;

        this.strategy = strategy;

        this.workHistory = new HashMap<>();
        this.contractHistory = new ArrayList<>();
        this.currentContract = null;
        this.nextContract = null;
        this.state = UNEMPLOYED;
    }

    @Override
    protected void setup() {
        Logger.info(getLocalName(), "Setup " + id);

        SequentialBehaviour sequential = new SequentialBehaviour(this);
        sequential.addSubBehaviour(new InitialEmployment());
        sequential.addSubBehaviour(new TechnicianNight());
        addBehaviour(sequential);
    }

    @Override
    protected void takeDown() {
        Logger.warn(getLocalName(), "Technician Terminated!");
    }

    public AID getHomeStation() {
        return homeStation;
    }

    public AID getStation() {
        return station;
    }

    public AID getCompany() {
        return company;
    }

    public WorkLog getWorkLog(int day) {
        return workHistory.get(day);
    }

    public Contract getPreviousContract() {
        if (contractHistory.isEmpty()) return null;
        return contractHistory.get(contractHistory.size() - 1);
    }

    public Contract getCurrentContract() {
        return currentContract;
    }

    public Contract getNextContract() {
        return nextContract;
    }

    public State getWorkState() {
        return state;
    }

    // ***** DATA

    private void createWorkLog(String worklog) {
        int day = World.get().getDay();
        int jobs = 0;    // ...
        double cut = 0;  // ...
        WorkLog log = new WorkLog(this, currentContract, jobs, cut);
        workHistory.put(day, log);
    }

    private void createEmptyWorkLog() {
        int day = World.get().getDay();
        WorkLog log = new WorkLog(this, null, 0, 0);
        workHistory.put(day, log);
    }

    // ***** BEHAVIOURS

    class MoveToNextContract extends OneShotBehaviour {
        private static final long serialVersionUID = -966288207328177898L;

        @Override
        public void action() {
            assert nextContract != null;
            if (currentContract != null) contractHistory.add(currentContract);

            currentContract = nextContract;
            nextContract = null;
            company = currentContract.company;
            station = currentContract.station;
        }
    }

    class FindNextContract extends OneShotBehaviour {
        private static final long serialVersionUID = 2433586834474062536L;

        @Override
        public void action() {
            if (nextContract != null || !strategy.lookForContracts()) return;

            Contract renewed = strategy.renewalContract();

            // Propose renewed...
        }
    }

    class TechnicianNight extends Behaviour {
        private static final long serialVersionUID = 3576074310971384343L;

        private void unemployedAction() {
            createEmptyWorkLog();
        }

        private void workingAction() {
            MessageTemplate acl, onto, mt;

            onto = MatchOntology("company-payment");
            acl = MatchPerformative(ACLMessage.INFORM);
            mt = and(and(onto, acl), MatchSender(company));
            ACLMessage inform = receive(mt);  // Protocol A
            while (inform == null) {
                block();
                inform = receive(mt);
            }
            String worklog = inform.getContent();
            createWorkLog(worklog);
        }

        @Override
        public void action() {
            if (state == UNEMPLOYED) {
                unemployedAction();
            } else {
                workingAction();
            }
        }

        @Override
        public boolean done() {
            return false;
            // return true on the final day
        }
    }

    private class InitialEmployment extends OneShotBehaviour {
        private static final long serialVersionUID = -8275421706452630634L;
        private static final String ontology = "initial-employment";

        @Override
        public void action() {
            MessageTemplate onto, acl, mt;

            ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
            message.setOntology(ontology);
            message.addReceiver(company);
            send(message);

            onto = MatchOntology(ontology);
            acl = MatchPerformative(ACLMessage.INFORM);
            mt = and(and(onto, acl), MatchSender(company));
            ACLMessage reply = receive(mt);
            while (reply == null) {
                block();
                reply = receive(mt);
            }

            String content = reply.getContent();

            // TODO LOGIC: store contract record

            state = WORKING;
            Logger.info(getLocalName(), "Initial employment @" + company.getLocalName());
        }
    }
}
