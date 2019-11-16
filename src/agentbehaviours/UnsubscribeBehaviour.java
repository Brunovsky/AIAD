package agentbehaviours;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UnsubscribeBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 5101487004586828719L;

    private final AID post;
    private final String ontology;

    public UnsubscribeBehaviour(Agent a, AID post, String ontology) {
        super(a);
        this.post = post;
        this.ontology = ontology;
    }

    @Override
    public void action() {
        MessageTemplate acl = MatchPerformative(ACLMessage.CONFIRM);
        MessageTemplate onto = MatchOntology(ontology);

        ACLMessage subscribe = new ACLMessage(ACLMessage.CANCEL);
        subscribe.setOntology(ontology);
        subscribe.addReceiver(post);
        myAgent.send(subscribe);

        ACLMessage confirm = myAgent.receive(and(onto, acl));
        while (confirm == null) {
            block();
            confirm = myAgent.receive(and(onto, acl));
        }
    }
}
