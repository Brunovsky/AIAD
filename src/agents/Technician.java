package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Logger;
import utils.TimeBoard;

public class Technician extends Agent {

    TimeBoard timeBoard;
    AID company;
    AID newCompany;

    public Technician(AID newCompany) {
        this.newCompany = newCompany;
        this.timeBoard = new TimeBoard();
    }

    protected void setup() {
        timeBoard = new TimeBoard();

        Logger.info(getLocalName(), "Setup Technician Agent");

        addBehaviour(new SubscribeCompany());
    }

    protected void takeDown() {



        Logger.warn(getLocalName(), "Technician Terminated!");
    }

    public TimeBoard getTimeBoard() {
        return timeBoard;
    }

    public class SubscribeCompany extends OneShotBehaviour {

        String ontology = "technician-subscription";
        
        @Override
        public void action() {
            MessageTemplate mt =  MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchOntology(ontology));
            
            ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
            message.setOntology(ontology);
            message.addReceiver(newCompany);
            send(message);
            
            ACLMessage responseMessage = null;

            while(responseMessage == null) {
                block();
                responseMessage = receive(mt);
            }

            String responseContent = responseMessage.getContent();

            // Change company

            Logger.info(myAgent.getLocalName(),"Received subscription confirmation from company: " + message.getSender());

            company = newCompany;

            //contractTime = received contract time
            //salary = salary received
            //repaircomission = received
            //contracttime
        }

    }
    
    public class UnsubscribeCompany extends OneShotBehaviour {

        String ontology = "company-subscription";
        
        @Override
        public void action() {
            MessageTemplate mt =  MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchOntology(ontology));
            
            ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
            message.setOntology(ontology);
            message.addReceiver(company);
            send(message);

//            while(response == null) {
//                block();
//                response = receive();
//            }
            ACLMessage responseMessage = blockingReceive(mt);

            String responseContent = responseMessage.getContent();



            Logger.info(myAgent.getLocalName(),"Received subscription confirmation from company: " + message.getSender());
            
        }

    }

}