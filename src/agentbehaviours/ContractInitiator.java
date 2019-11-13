package agentbehaviours;

import agents.Client;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import message.TechnicianMessage;
import utils.Logger;

import java.util.Enumeration;
import java.util.Vector;

public class ContractInitiator extends ContractNetInitiator {

    private int nResponders;
    private DFAgentDescription[] agents;

    public ContractInitiator(Agent a, ACLMessage msg, DFAgentDescription[] agents) {
        super(a, msg);
        this.agents = agents;
        this.nResponders = agents.length;
        // ((Client) myAgent).getLocation()
    }

    // Warning: Useless function because we will use handleAllResponses
    @Override
    protected void handlePropose(ACLMessage propose, Vector v) {
        try {
            Logger.info(myAgent.getLocalName(), "Agent " + propose.getSender().getName() + " proposed " + propose.getContentObject());
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        Logger.warn(myAgent.getLocalName(), "Agent " + refuse.getSender().getName() + " refused");
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            Logger.error(myAgent.getLocalName(), "Responder does not exist");
        } else {
            Logger.error(myAgent.getLocalName(), "Agent " + failure.getSender().getName() + " failed");
        }
        // Immediate failure --> we will not receive a response from this agent
        nResponders--;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        // Next if will be deleted probably
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            Logger.warn(myAgent.getLocalName(), "Timeout expired: missing " + (nResponders - responses.size()) + " responses");
        }

        // Evaluate proposals.
        TechnicianMessage bestProposal = null;
        AID bestProposer = null;

        ACLMessage accept = null;
        Enumeration e = responses.elements();
        // Iterate through all responses
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.addElement(reply);
                TechnicianMessage proposal = null;
                try {
                    proposal = (TechnicianMessage) msg.getContentObject();
                } catch (UnreadableException e1) {
                    e1.printStackTrace();
                }
                if (proposal != null && ((Client) myAgent).compareTechnicianMessages(proposal, bestProposal)) {
                    bestProposal = proposal;
                    bestProposer = msg.getSender();
                    accept = reply;
                }
            }
        }

        // Accept the proposal of the best proposer
        if (accept != null) {
            Logger.info(myAgent.getLocalName(),"Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        Logger.info(myAgent.getLocalName(), "Agent " + inform.getSender().getName() + " successfully performed the requested action");
    }


}
