package agentbehaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WaitRepairRequests extends CyclicBehaviour {
    @Override
    public void action() {
        //System.out.println("ANOTHER BEHAVIOUR");
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP) );

        myAgent.addBehaviour(new CoolBehaviour(myAgent, template));
    }

}
