package agentbehaviours;

import agents.Client;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import message.ClientMessage;

import java.io.IOException;


public class RequestRepair extends Behaviour {

    private DFAgentDescription[] agents;
    private int nResponders;
    private boolean finished = false;

    public RequestRepair(DFAgentDescription[] agents) {
        this.agents = agents;
        this.nResponders = agents.length;
        this.finished = false;
    }

    public String getAgentName(String aid) {
        int pos = aid.indexOf("@");
        return aid.substring(0, pos);
    }

    @Override
    public void action() {
        // Fill the CFP message
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (int i = 0; i < agents.length; ++i) {
            msg.addReceiver(new AID(getAgentName(agents[i].getName().getName()), AID.ISLOCALNAME));
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

        // We want to receive a reply in 10 secs
        //msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

        ClientMessage messageToBeSent = new ClientMessage(((Client) myAgent).getLocation(), ((Client) myAgent).getMalfunctionType(), ((Client) myAgent).getRequestSendTime());

        try {
            msg.setContentObject(messageToBeSent);
        } catch (IOException e) {
            e.printStackTrace();
        }


        myAgent.addBehaviour(new ContractInitiator(myAgent, msg, agents));
    }

    @Override
    public boolean done() {
        // TODO: check when it's finished
        this.finished = true;
        return finished;
    }


}
