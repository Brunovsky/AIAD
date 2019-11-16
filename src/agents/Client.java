package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;
import static message.Message.getClientMalFunctionRequestMessage;

import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import message.ClientRequest;
import utils.Logger;

public class Client extends Agent {
    private static final long serialVersionUID = 5090227891936996896L;

    private final String id;
    private AID station;

    public Client(String id, AID station) {
        assert id != null && station != null;
        this.id = id;
        this.station = station;
    }

    @Override
    protected void setup() {
        Logger.info(getLocalName(), "Setup");

        SequentialBehaviour sequential = new SequentialBehaviour(this);
        sequential.addSubBehaviour(new ClientSubscribe());
        sequential.addSubBehaviour(new ClientNight());
        sequential.addSubBehaviour(new ClientUnsubscribe());

        addBehaviour(sequential);
    }

    @Override
    protected void takeDown() {
        Logger.warn(getLocalName(), "Client Terminated!");
    }

    private HashMap<Integer, ClientRequest> generateNewRepairs() {
        return new HashMap<>();
    }

    private HashMap<Integer, Double> evaluateAdjustments() {
        return new HashMap<>();
    }

    class ClientNight extends Behaviour {
        private static final long serialVersionUID = 2838271060454701293L;

        @Override
        public void action() {
            MessageTemplate acl, onto;

            HashMap<Integer, ClientRequest> repairs = generateNewRepairs();
            HashMap<Integer, Double> adjustments = evaluateAdjustments();

            // wait for request message
            onto = MatchOntology("prompt-client-malfunctions");
            acl = MatchPerformative(ACLMessage.REQUEST);
            ACLMessage request = receive(and(onto, acl));
            while (request == null) {
                block();
                request = receive(and(onto, acl));
            }

            // answer message
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(getClientMalFunctionRequestMessage(repairs, adjustments));
            send(reply);

            // wait for assignments...
            onto = MatchOntology("inform-client-assignment");
            acl = MatchPerformative(ACLMessage.INFORM);
            ACLMessage assign = receive(and(onto, acl));
            while (assign == null) {
                block();
                assign = receive(and(onto, acl));
            }

            // Remove informed malfunctions which will be solved in the next day.
            // Process the informed prices: consider increasing/decreasing maximum prices set.
        }

        @Override
        public boolean done() {
            return true;
            // return false in the final day to unsubscribe.
        }
    }

    class ClientSubscribe extends OneShotBehaviour {
        private static final long serialVersionUID = -3848576838699802376L;

        @Override
        public void action() {
            MessageTemplate acl = MatchPerformative(ACLMessage.CONFIRM);
            MessageTemplate onto = MatchOntology("client-subscription");

            ACLMessage subscribe = new ACLMessage(ACLMessage.SUBSCRIBE);
            subscribe.setOntology("client-subscription");
            subscribe.addReceiver(station);
            send(subscribe);

            ACLMessage confirm = receive(and(onto, acl));
            while (confirm == null) {
                block();
                confirm = receive(and(onto, acl));
            }
        }
    }

    class ClientUnsubscribe extends OneShotBehaviour {
        private static final long serialVersionUID = 2345418834603455376L;

        @Override
        public void action() {
            MessageTemplate confirm = MatchPerformative(ACLMessage.CONFIRM);
            MessageTemplate onto = MatchOntology("client-subscription");
            MessageTemplate mt = and(confirm, onto);

            ACLMessage subscribe = new ACLMessage(ACLMessage.CANCEL);
            subscribe.setOntology("client-subscription");
            subscribe.addReceiver(station);
            send(subscribe);

            ACLMessage reply = receive(mt);
            if (reply == null) {
                Logger.warn(getLocalName(), "Did not receive unsubscription confirmation");
            }
        }
    }
}
