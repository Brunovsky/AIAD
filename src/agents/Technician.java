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

import agentbehaviours.AwaitDayBehaviour;
import agentbehaviours.AwaitNightBehaviour;
import agentbehaviours.SequentialLoopBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.World;
import strategies.TechnicianStrategy;
import types.Contract;
import types.WorkFinance;
import types.WorkLog;
import utils.Logger;

public class Technician extends Agent {
    private static final long serialVersionUID = 2763283727137639385L;

    private final String id;
    private final AID homeStation;
    private AID company;

    private final Map<Integer, WorkLog> workHistory;
    private final ArrayList<Contract> contractHistory;
    private Contract currentContract, nextContract;
    private State state;

    private final TechnicianStrategy strategy;

    public enum State { WORKING, UNEMPLOYED }

    public Technician(String id, AID homeStation, AID company, TechnicianStrategy strategy) {
        assert id != null && homeStation != null && company != null && strategy != null;
        this.id = id;
        this.homeStation = homeStation;
        this.company = company;

        this.workHistory = new HashMap<>();
        this.contractHistory = new ArrayList<>();
        this.currentContract = null;
        this.nextContract = null;
        this.state = UNEMPLOYED;

        this.strategy = strategy;
        strategy.setTechnician(this);
    }

    @Override
    protected void setup() {
        Logger.technician(id, "Setup " + id);

        // Setup
        addBehaviour(new InitialEmployment(this));

        SequentialLoopBehaviour loop = new SequentialLoopBehaviour(this);
        // Night
        loop.addSubBehaviour(new AwaitNightBehaviour(this));
        loop.addSubBehaviour(new TechnicianNight(this));
        // Day
        loop.addSubBehaviour(new AwaitDayBehaviour(this));
        loop.addSubBehaviour(new ProposeRenewal(this));
        loop.addSubBehaviour(new MoveToNextContract(this));
        addBehaviour(loop);
    }

    @Override
    protected void takeDown() {
        Logger.technician(id, "Technician Terminated!");
        StringBuilder builder = new StringBuilder();
        double earned = 0.0;
        for (int day : workHistory.keySet()) {
            WorkLog log = workHistory.get(day);
            earned += log.finance.earned;
            builder.append(String.format("%d %d %f\n", day, log.state == WORKING ? 1 : 0, earned));
        }
        Logger.write(id, builder.toString());
    }

    public AID getHomeStation() {
        return homeStation;
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

    private void createWorkLog(WorkFinance finance) {
        int day = World.get().getDay();
        assert finance.salary == currentContract.salary;
        WorkLog log = new WorkLog(this, currentContract, finance);
        workHistory.put(day, log);
    }

    private void createEmptyWorkLog() {
        int day = World.get().getDay();
        WorkLog log = new WorkLog(this);
        workHistory.put(day, log);
    }

    // ***** BEHAVIOURS

    private class ProposeRenewal extends OneShotBehaviour {
        private static final long serialVersionUID = 2433586834474062536L;

        ProposeRenewal(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            if (nextContract != null || !strategy.lookForContracts()) return;

            Contract renewed = strategy.renewalContract();

            ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
            message.setOntology(World.get().getTechnicianOfferContract());
            message.setContent(renewed.make());
            send(message);

            Logger.technician(id, "Renewing with company " + company.getLocalName());

            // TODO SIMPLIFICATION
            MessageTemplate onto = MatchOntology(World.get().getTechnicianOfferContract());
            MessageTemplate acl = MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            /* ACLMessage reply = */ blockingReceive(and(onto, acl));

            contractHistory.add(renewed);
            nextContract = renewed;
        }
    }

    private class MoveToNextContract extends OneShotBehaviour {
        private static final long serialVersionUID = -966288207328177898L;

        MoveToNextContract(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            int day = World.get().getDay();

            if (currentContract != null && day == currentContract.end) {
                contractHistory.add(currentContract);
                currentContract = null;
                state = UNEMPLOYED;
            }

            if (nextContract != null && day + 1 == nextContract.start) {
                currentContract = nextContract;
                nextContract = null;
                state = WORKING;
            }
        }
    }

    private class TechnicianNight extends OneShotBehaviour {
        private static final long serialVersionUID = 3576074310971384343L;

        TechnicianNight(Agent a) {
            super(a);
        }

        private void unemployedAction() {
            createEmptyWorkLog();
        }

        private void workingAction() {
            MessageTemplate acl = MatchPerformative(ACLMessage.INFORM);
            MessageTemplate onto = MatchOntology(World.get().getCompanyPayment());
            MessageTemplate mt = and(and(onto, acl), MatchSender(company));
            ACLMessage inform = blockingReceive(mt);  // Protocol A
            WorkFinance finance = WorkFinance.from(inform);
            createWorkLog(finance);

            Logger.technician(id, "Received payment from company, value of " + finance.earned);
        }

        @Override
        public void action() {
            if (state == UNEMPLOYED) {
                unemployedAction();
            } else {
                workingAction();
            }
        }
    }

    private class InitialEmployment extends OneShotBehaviour {
        private static final long serialVersionUID = -8275421706452630634L;
        private final String ontology;

        InitialEmployment(Agent a) {
            super(a);
            this.ontology = World.get().getInitialEmployment();
        }

        @Override
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
            message.setOntology(ontology);
            message.addReceiver(company);
            message.setContent(homeStation.getLocalName());
            send(message);

            MessageTemplate onto = MatchOntology(ontology);
            MessageTemplate acl = MatchPerformative(ACLMessage.INFORM);
            ACLMessage reply = blockingReceive(and(and(onto, acl), MatchSender(company)));

            Contract contract = Contract.from(company, myAgent.getAID(), reply);
            currentContract = contract;
            state = WORKING;

            Logger.technician(id, "Initial employment @" + company.getLocalName());
        }
    }
}
