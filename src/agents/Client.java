package agents;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Logger;

public class Client extends Agent {
    private static final long serialVersionUID = 5090227891936996896L;

    private String id;
    private AID station;

    public Client(String id, AID station) {
        assert id != null && station != null;
        this.id = id;
        this.station = station;
    }

    @Override
    public void setup() {
        Logger.info(getLocalName(), "Setup");

        SequentialBehaviour sequential = new SequentialBehaviour(this);
        sequential.addSubBehaviour(new ClientSubscribe());
        sequential.addSubBehaviour(new ClientDay());
        sequential.addSubBehaviour(new ClientUnsubscribe());

        addBehaviour(sequential);
    }

    private String generateNewRepairs() {
        // ...
        return "wingardium";
    }

    private String evaluateAdjustments() {
        // ...
        return "leviosa";
    }

    class ClientDay extends CyclicBehaviour {
        private static final long serialVersionUID = 2838271060454701293L;

        @Override
        public void action() {
            MessageTemplate acl, onto;

            String repairs = generateNewRepairs();
            String adjustments = evaluateAdjustments();

            // wait for request message
            onto = MatchOntology("prompt-client-malfunctions");
            acl = MatchPerformative(ACLMessage.REQUEST);
            ACLMessage request = receive(and(onto, acl));

            // answer message
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(repairs + "\n" + adjustments);
            send(reply);

            // wait for assignments...
            onto = MatchOntology("inform-client-assignment");
            acl = MatchPerformative(ACLMessage.INFORM);
            ACLMessage assign = receive(and(onto, acl));

            // Remove informed malfunctions which will be solved in the next day.
            // Process the informed prices: consider increasing/decreasing maximum prices set.
        }
    }

    class ClientSubscribe extends OneShotBehaviour {
        private static final long serialVersionUID = -3848576838699802376L;

        @Override
        public void action() {
            MessageTemplate confirm = MatchPerformative(ACLMessage.CONFIRM);
            MessageTemplate onto = MatchOntology("client-subscription");
            MessageTemplate mt = and(confirm, onto);

            ACLMessage subscribe = new ACLMessage(ACLMessage.SUBSCRIBE);
            subscribe.setOntology("client-subscription");
            subscribe.addReceiver(station);
            send(subscribe);

            ACLMessage reply = receive(mt);
            if (reply == null) {
                Logger.warn(getLocalName(), "Did not receive subscription confirmation");
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
