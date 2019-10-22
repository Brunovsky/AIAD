package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.util.leap.Iterator;
import jade.proto.SubscriptionInitiator;
import jade.lang.acl.ACLMessage;

public class Client extends Agent {

  protected void setup() {

	String serviceType = "tech-repairs";
		  
  		// Build the description used as template for the subscribe
  		DFAgentDescription template = new DFAgentDescription();
  		ServiceDescription templateSd = new ServiceDescription();
		//templateSd.setName("TechRepairs");
  		templateSd.setType(serviceType);
  		template.addServices(templateSd);
  		
  		SearchConstraints sc = new SearchConstraints();
  		// We want to receive 100 results at most
  		sc.setMaxResults(new Long(100));

		// Subscribe

  		System.out.println("Agent "+getLocalName()+" subscribing for services of type \"" + serviceType + "\"");

		addBehaviour(new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, sc)) {
			protected void handleInform(ACLMessage inform) {
  			System.out.println("Agent "+getLocalName()+": Notification received from DF");
  			try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
		  		if (results.length > 0) {
		  			for (int i = 0; i < results.length; ++i) {
		  				DFAgentDescription dfd = results[i];
		  				AID provider = dfd.getName();
		  				// The same agent may provide several services; we are only interested
		  				// in the tech-repairs one
		  				Iterator it = dfd.getAllServices();
		  				while (it.hasNext()) {
		  					ServiceDescription sd = (ServiceDescription) it.next();
		  					if (sd.getType().equals(serviceType)) {
	  							System.out.println(serviceType + " service for found:");
		  						System.out.println("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
		  					}
		  				}
		  			}
		  		}	
	  			System.out.println();
		  	}
		  	catch (FIPAException fe) {
		  		fe.printStackTrace();
		  	}
			}
		} );
  		
		  /*
  		DFAgentDescription[] results = DFService.search(this, template); // sc

		  //	Blocks for 5 seconds
  		//DFAgentDescription[] results = DFService.searchUntilFound(this, this.getName(), template, sc, 1000*5);
		  
  		if (results != null && results.length > 0) {
  			System.out.println("Agent "+getLocalName()+" found the following tech-repairs services:");
  			for (int i = 0; i < results.length; ++i) {
  				DFAgentDescription dfd = results[i];
  				AID provider = dfd.getName();
  				// The same agent may provide several services; we are only interested
  				// in the weather-forcast one
  				Iterator it = dfd.getAllServices();
  				while (it.hasNext()) {
  					ServiceDescription sd = (ServiceDescription) it.next();
  					if (sd.getType().equals("tech-repairs")) {
  						System.out.println("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
  					}
  				}
  			}
  		}
  		else {
  			System.out.println("Agent "+getLocalName()+" did not find any tech-repairs service");
  		}
	 */ 
  	
  } 
}

