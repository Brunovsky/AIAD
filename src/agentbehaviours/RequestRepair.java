package agentbehaviours;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;


public class RequestRepair extends Behaviour {

    private DFAgentDescription[] agents;
    private int nResponders;
    private boolean finished = false;

    public RequestRepair(DFAgentDescription[] agents) {
        this.agents = agents;
        this.nResponders = agents.length;
        this.finished = false;
        System.out.println(agents);
    }

    public String getAgentName(String aid) {
        int pos = aid.indexOf("@");
        return aid.substring(0, pos);
    }

    @Override
    public void action() {
        //System.out.println("SOME BEHAVIOUR");

        // Fill the CFP message
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (int i = 0; i < agents.length; ++i) {
            System.out.println(agents[i].getName().getName());
            System.out.println(new AID((String) getAgentName(agents[i].getName().getName()), AID.ISLOCALNAME));
            msg.addReceiver(new AID((String) getAgentName(agents[i].getName().getName()), AID.ISLOCALNAME));
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        //msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        msg.setContent("O Miguel é giro");

        myAgent.addBehaviour(new ContractInitiator(myAgent, msg, agents));
    }

    @Override
    public boolean done() {
        this.finished = true;
        return finished;
    }


}
