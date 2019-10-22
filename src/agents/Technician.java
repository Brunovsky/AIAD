package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;

/**
*		args[] -> name, x, y
*/
public class Technician extends Agent {

	//private Random rand;	//	for random location

	//	We need to create a map and decide a range. tipo: 100x100
	//private Pair<x,y> location;	Use pairs or x and y?

	private int x;
	private int y;

  protected void setup() {
  	String serviceName = "TechRepairs";
  	
  	// Read the name of the service to register as an argument
  	Object[] args = getArguments();
  	if (args != null) {
			switch(args.length){
				case 2:	// Supose its x and y
					x = tryParse(args[0]);
					y = tryParse(args[1]);
					break;
				default:
					System.out.println("Usage agent:xcoordinate:ycoordinate");
					System.out.println("Creating agent with random coordinates.");
  		}
				
		}
		

  	
  	// Register the service
  	System.out.println("Agent "+getLocalName()+" registering service \""+serviceName+"\" of type \"tech-repairs\"");

  	try {
  		DFAgentDescription dfd = new DFAgentDescription();
  		dfd.setName(getAID());
  		ServiceDescription sd = new ServiceDescription();
  		sd.setName(serviceName);
  		sd.setType("tech-repairs");
  		// Agents that want to use this service need to "know" the tech-repairs-ontology
  		sd.addOntologies("tech-repairs-ontology");
  		// Agents that want to use this service need to "speak" the FIPA-SL language
  		sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
  		sd.addProperties(new Property("country", "Italy"));
  		dfd.addServices(sd);
  		
  		DFService.register(this, dfd);
  	}
  	catch (FIPAException fe) {
  		fe.printStackTrace();
  	}
  } 
 
  /*
    Agent Termination
  */
  protected void takeDown(){

    // Removing the registration in the yellow pages
    try{
      DFService.deregister(this);
    }
    catch(FIPAException fe){
      fe.printStackTrace();
    }

    System.out.println("Technician-agent " + getLocalName() + " has terminated!");
  } 
	
	public static Integer tryParse(String text)
	{
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			System.out.println("Wrong coordinates format.");
			System.out.println("Generating random coordinates.");
			Random rand;
			return rand.nextInt(101);
  }
}

}

