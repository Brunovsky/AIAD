package agentbehaviours;

import agents.Technician;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import message.ClientMessage;
import message.TechnicianMessage;

import java.io.IOException;

public class ContractResponder extends ContractNetResponder {

    private Agent agent;

    public ContractResponder(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agent = a;

    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        try {
            System.out.println("Agent "+agent.getLocalName()+": CFP received from "+cfp.getSender().getName());
            //System.out.println(((ClientMessage) cfp.getContentObject()).getLocation());
            System.out.println("Message: "+cfp.getContentObject());


            TechnicianMessage proposal =((Technician) myAgent).handleReceivedClientCfp((ClientMessage) cfp.getContentObject());
            if(proposal != null){
                // Send propose to Client
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContentObject(proposal);
                return propose;
            } else {
                // Don't send propose to Client
                System.out.println("Agent "+agent.getLocalName()+": Refuse");
            }
        } catch (UnreadableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RefuseException("evaluation-failed");
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
        System.out.println("Agent "+agent.getLocalName()+": Proposal accepted");
        if (((Technician) myAgent).handleReceivedClientAcceptProposal(cfp, propose)) {
            System.out.println("Agent "+agent.getLocalName()+": Action successfully performed");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
        else {
            System.out.println("Agent "+agent.getLocalName()+": Action execution failed");
            throw new FailureException("unexpected-error");
        }
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println("Agent "+agent.getLocalName()+": Proposal rejected");
    }
}
