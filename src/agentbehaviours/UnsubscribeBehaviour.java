package agentbehaviours;

import static jade.lang.acl.MessageTemplate.MatchOntology;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.and;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UnsubscribeBehaviour extends WaitingBehaviour {
    private static final long serialVersionUID = 5101487004586828719L;

    private final AID post;
    private final String ontology;

    private boolean unsubscribed = false;

    public UnsubscribeBehaviour(Agent a, AID post, String ontology) {
        super(a);
        this.post = post;
        this.ontology = ontology;
    }

    @Override
    public void action() {
        if (!unsubscribed) {
            ACLMessage subscribe = new ACLMessage(ACLMessage.CANCEL);
            subscribe.setOntology(ontology);
            subscribe.addReceiver(post);
            myAgent.send(subscribe);
            unsubscribed = true;
        }

        MessageTemplate acl = MatchPerformative(ACLMessage.CONFIRM);
        MessageTemplate onto = MatchOntology(ontology);
        ACLMessage confirm = myAgent.receive(and(onto, acl));
        if (confirm != null)
            finalize();
        else
            block();
    }
}
