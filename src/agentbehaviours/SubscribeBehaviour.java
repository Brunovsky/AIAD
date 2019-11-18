package agentbehaviours;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Logger;

public class SubscribeBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 5101487004586828719L;

    private final AID post;
    private final String ontology;

    public SubscribeBehaviour(Agent a, AID post, String ontology) {
        super(a);
        this.post = post;
        this.ontology = ontology;
    }

    @Override
    public void action() {
        MessageTemplate acl = MatchPerformative(ACLMessage.CONFIRM);
        MessageTemplate onto = MatchOntology(ontology);

        ACLMessage subscribe = new ACLMessage(ACLMessage.SUBSCRIBE);
        subscribe.setOntology(ontology);
        subscribe.addReceiver(post);
        myAgent.send(subscribe);

        Logger.error(myAgent.getLocalName(),
                     "[" + ontology + "] Subscribing to " + post.getLocalName());

        ACLMessage confirm = myAgent.receive(and(onto, acl));
        while (confirm == null) {
            block();
            confirm = myAgent.receive(and(onto, acl));
        }

        Logger.error(myAgent.getLocalName(),
                     "[" + ontology + "] Subscribed to " + post.getLocalName());
    }
}
