package agentbehaviours;

import java.util.Enumeration;
import java.util.Vector;

import agents.Client;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import message.TechnicianMessage;
import utils.Logger;

public class ContractInitiator extends ContractNetInitiator {
    private int nResponders;
    private DFAgentDescription[] agents;

    final Client myClient;

    public ContractInitiator(Agent a, ACLMessage msg, DFAgentDescription[] agents) {
        super(a, msg);
        this.agents = agents;
        this.nResponders = agents.length;
        this.myClient = (Client) myAgent;
    }

    // Warning: Useless function because we will use handleAllResponses
    //  Can be used for logging or smthg like that.
    @Override
    protected void handlePropose(ACLMessage propose, Vector v) {
        String technicianName = propose.getSender().getLocalName();
        Logger.info(myAgent.getLocalName(), "Agent " + technicianName + " proposed");
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        String technicianName = refuse.getSender().getLocalName();
        Logger.warn(myAgent.getLocalName(), "Agent " + technicianName + " refused");
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        // Next if will be deleted probably
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            Logger.warn(myAgent.getLocalName(), "Timeout expired: missing "
                                                    + (nResponders - responses.size())
                                                    + " responses");
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
                    if (proposal == null) {
                        throw new RuntimeException("Proposal from technician is null");
                    }
                } catch (UnreadableException e1) {
                    e1.printStackTrace();
                }
                if (bestProposal == null
                    || myClient.compareTechnicianMessages(proposal, bestProposal)) {
                    bestProposal = proposal;
                    bestProposer = msg.getSender();
                    accept = reply;
                }
            }
        }

        // Accept the proposal of the best proposer
        if (accept != null) {
            Logger.info(myAgent.getLocalName(),
                        "Accepting proposal from " + bestProposer.getLocalName());
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        String technicianName = failure.getSender().getLocalName();
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            Logger.error(myAgent.getLocalName(), "Responder does not exist");
        } else {
            Logger.error(myAgent.getLocalName(), "Agent " + technicianName + " failed");
        }
        // Immediate failure --> we will not receive a response from this agent
        nResponders--;
        myAgent.doDelete();
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        String technicianName = inform.getSender().getName();
        Logger.info(myAgent.getLocalName(),
                    "Agent " + technicianName + " successfully performed the requested action");
        myAgent.doDelete();
    }
}
