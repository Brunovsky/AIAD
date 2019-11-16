package agents;

import static agents.Technician.State.UNEMPLOYED;
import static agents.Technician.State.WORKING;
import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;

import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import simulation.World;
import types.WorkLog;
import utils.Logger;

public class Technician extends Agent {
    private static final long serialVersionUID = 2763283727137639385L;

    private final String id;
    private final AID homeStation;
    private final AID company, station;

    private final Map<Integer, WorkLog> history;
    private State state;

    public enum State { WORKING, MOVING, UNEMPLOYED }

    public Technician(String id, AID homeStation, AID company) {
        assert id != null && homeStation != null && company != null;
        this.id = id;
        this.homeStation = homeStation;
        this.company = company;
        this.station = homeStation;

        this.history = new HashMap<>();
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

    public State getWorkState() {
        return state;
    }

    public double getSalary() {
        // TODO LOGIC
        return 1337.0;
    }

    // ***** DATA

    private void createWorkLog(String worklog) {
        int day = World.get().getDay();
        int jobs = 0;    // ...
        double cut = 0;  // ...
        WorkLog log = new WorkLog(this, jobs, cut);
        history.put(day, log);
    }

    // ***** BEHAVIOURS

    class TechnicianNight extends Behaviour {
        private static final long serialVersionUID = 3576074310971384343L;

        @Override
        public void action() {
            // No job and no pay if no company. TODO: this is not correctly implemented.
            if (company == null) return;

            MessageTemplate acl, onto, mt;

            onto = MatchOntology("company-playment");
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
