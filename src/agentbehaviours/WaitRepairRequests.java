package agentbehaviours;

import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WaitRepairRequests extends Behaviour {
    @Override
    public void action() {
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP) );

        myAgent.addBehaviour(new ContractResponder(myAgent, template));
    }

    @Override
    public boolean done() {
        return true;
    }

}
