package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;

import java.util.HashSet;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Logger;
import utils.TimeBoard;

public class Technician extends Agent {
    private static final long serialVersionUID = 2763283727137639385L;

    private final String id;
    private TimeBoard timeBoard;
    private AID company;
    private AID newCompany;

    private AID homeStation;
    private AID station, leader;
    private Set<AID> subordinates;
    private State state;

    public Technician(String id, AID homeStation, AID newCompany) {
        assert id != null && newCompany != null;
        this.id = id;
        this.newCompany = newCompany;
        this.timeBoard = new TimeBoard();

        this.homeStation = homeStation;
        this.station = null;
        this.leader = null;
        this.subordinates = new HashSet<>();
        this.state = State.UNEMPLOYED;
    }

    protected void setup() {
        Logger.info(getLocalName(), "Setup");

        SequentialBehaviour sequential = new SequentialBehaviour(this);
        sequential.addSubBehaviour(new SubscribeCompany());
        sequential.addSubBehaviour(new TechnicianNight());
        sequential.addSubBehaviour(new UnsubscribeCompany());
        addBehaviour(sequential);
    }

    protected void takeDown() {
        Logger.warn(getLocalName(), "Technician Terminated!");
    }

    public TimeBoard getTimeBoard() {
        return timeBoard;
    }

    private enum State { LEADING, WORKING, MOVING, UNEMPLOYED }

    class TechnicianNight extends Behaviour {
        private static final long serialVersionUID = 3576074310971384343L;

        private void leaderAction() {
            MessageTemplate acl, onto, mt;

            MessageTemplate fromStation = MatchSender(station);
            onto = MatchOntology("inform-technician-jobs");
            acl = MatchPerformative(ACLMessage.REQUEST);
            mt = and(and(onto, acl), fromStation);
            ACLMessage stationRequest = receive(mt);
            while (stationRequest != null) {
                block();
                stationRequest = receive(mt);
            }

            // TODO LOGIC: generate subordinates.size() allocations for all subordinates.
            // Use the company strategy or the leader's strategy.
            String[] allocations = new String[subordinates.size()];

            int i = 0;
            for (AID subordinate : subordinates) {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setOntology("technician-assign-job");
                message.addReceiver(subordinate);
                message.setContent(allocations[i++]);
                send(message);
            }

            ACLMessage refuse = stationRequest.createReply();
            refuse.setPerformative(ACLMessage.REFUSE);
            send(refuse);

            onto = MatchOntology("technician-assign-job");
            acl = MatchPerformative(ACLMessage.CONFIRM);
            mt = and(onto, acl);

            for (AID subordinate : subordinates) {
                MessageTemplate from = and(mt, MatchSender(subordinate));
                ACLMessage confirm = receive(from);
                while (confirm == null) {
                    block();
                    confirm = receive(from);
                }
            }
        }

        private void workerAction() {
            MessageTemplate acl, onto, mt;

            MessageTemplate fromStation = MatchSender(station);
            onto = MatchOntology("inform-technician-jobs");
            acl = MatchPerformative(ACLMessage.REQUEST);
            mt = and(and(onto, acl), fromStation);
            ACLMessage stationRequest = receive(mt);
            while (stationRequest != null) {
                block();
                stationRequest = receive(mt);
            }

            MessageTemplate fromLeader = MatchSender(leader);
            onto = MatchOntology("technician-assign-job");
            acl = MatchPerformative(ACLMessage.INFORM);
            mt = and(and(onto, acl), fromLeader);
            ACLMessage leaderAdvice = receive(mt);
            while (leaderAdvice == null) {
                block();
                leaderAdvice = receive(mt);
            }
            String content = leaderAdvice.getContent();

            ACLMessage inform = stationRequest.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            inform.setContent(content);
            send(inform);

            ACLMessage confirm = leaderAdvice.createReply();
            confirm.setPerformative(ACLMessage.CONFIRM);
            send(confirm);

            onto = MatchOntology("inform-technician-assignment");
            acl = MatchPerformative(ACLMessage.INFORM);
            mt = and(and(onto, acl), fromStation);
            ACLMessage accepted = receive(mt);

            // ...
        }

        private void moverAction() {
            addBehaviour(new TechnicianStationUnsubscribe(station));
            station = null;
            leader = null;
        }

        @Override
        public void action() {
            /** NIGHT **/
            if (state == State.WORKING) {
                workerAction();
            } else if (state == State.LEADING) {
                leaderAction();
            } else if (state == State.MOVING) {
                moverAction();
            }

            /** DAY **/
        }

        @Override
        public boolean done() {
            return false;
        }
    }

    class SubscribeCompany extends OneShotBehaviour {
        private static final long serialVersionUID = -8275431706452650634L;

        private static final String ontology = "technician-station-subscription";

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(
                                                         ACLMessage.CONFIRM),
                                                     MessageTemplate.MatchOntology(ontology));

            ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
            message.setOntology(ontology);
            message.addReceiver(newCompany);
            send(message);

            ACLMessage responseMessage = null;

            while (responseMessage == null) {
                block();
                responseMessage = receive(mt);
            }

            String responseContent = responseMessage.getContent();

            // Change company

            Logger.info(myAgent.getLocalName(),
                        "Received subscription confirmation from company: " + message.getSender());

            company = newCompany;

            // contractTime = received contract time
            // salary = salary received
            // repaircomission = received
            // contracttime
        }
    }

    class UnsubscribeCompany extends OneShotBehaviour {
        private static final long serialVersionUID = 6478009297894126000L;

        private static final String ontology = "company-subscription";

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(
                                                         ACLMessage.CONFIRM),
                                                     MessageTemplate.MatchOntology(ontology));

            ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
            message.setOntology(ontology);
            message.addReceiver(company);
            send(message);

            ACLMessage response = blockingReceive(mt);

            while (response == null) {
                block();
                response = receive();
            }

            String responseContent = response.getContent();

            Logger.info(myAgent.getLocalName(),
                        "Received subscription confirmation from company: " + message.getSender());
        }
    }

    class TechnicianStationSubscribe extends OneShotBehaviour {
        private static final long serialVersionUID = 369262029263849583L;

        private AID station;

        TechnicianStationSubscribe(AID station) {
            this.station = station;
        }

        @Override
        public void action() {
            MessageTemplate acl = MatchPerformative(ACLMessage.CONFIRM);
            MessageTemplate onto = MatchOntology("technician-station-subscription");

            ACLMessage subscribe = new ACLMessage(ACLMessage.SUBSCRIBE);
            subscribe.setOntology("technician-station-subscription");
            subscribe.addReceiver(station);
            send(subscribe);

            ACLMessage confirm = receive(and(onto, acl));
            while (confirm == null) {
                block();
                confirm = receive(and(onto, acl));
            }
        }
    }

    class TechnicianStationUnsubscribe extends OneShotBehaviour {
        private static final long serialVersionUID = 369262029263849583L;

        private AID station;

        TechnicianStationUnsubscribe(AID station) {
            this.station = station;
        }

        @Override
        public void action() {
            MessageTemplate acl = MatchPerformative(ACLMessage.CONFIRM);
            MessageTemplate onto = MatchOntology("technician-station-subscription");

            ACLMessage subscribe = new ACLMessage(ACLMessage.CANCEL);
            subscribe.setOntology("technician-station-subscription");
            subscribe.addReceiver(station);
            send(subscribe);

            ACLMessage confirm = receive(and(onto, acl));
            while (confirm == null) {
                block();
                confirm = receive(and(onto, acl));
            }
        }
    }
}
