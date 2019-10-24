package agents;

import agentbehaviours.RequestRepair;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.leap.Iterator;
import message.TechnicianMessage;
import utils.ClientType;
import utils.Location;
import utils.Utils;

import static java.lang.System.exit;

public class Client extends Agent {

	// TODO: Create different classes that extends Client in order to have different types of clients and remove Utils.java

	private Location location;
	private ClientType type;

	protected void setup() {
        System.out.println("Setup Client Agent");
		String serviceType = "tech-repairs";

		// TODO: get args from console

		String[] args = (String[]) getArguments();
		if(args != null && args.length == 3){
			location = new Location(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			type = Utils.getClientType(args[2]);

			if(type == null){
				System.out.println("Wrong arguments");
				exit(0);
			}
		} else {
			System.out.println("Wrong arguments");
			exit(0);
		}
		System.out.println(location.getX());
		System.out.println(location.getY());
		//		int x = 0;
//		int y = 0;



//		location = new Location(x, y);
//		type = ClientType.REASONABLE_UNAVAILABLE;

		// Use myAgent to access Client private variables


		System.out.println("Agent "+getLocalName()+" searching for services of type " + serviceType);

		try {
			// Build the description used as template for the search
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType(serviceType);
			template.addServices(templateSd);

			// Constraint for search
			//SearchConstraints sc = new SearchConstraints();
			//sc.setMaxResults(new Long(100));

			//DFAgentDescription[] results = DFService.search(this, template, sc);
            DFAgentDescription[] results = DFService.search(this, template);

			if (results.length > 0) {
				System.out.println("Agent "+getLocalName()+" found the following "+serviceType+" services:");

				for (int i = 0; i < results.length; ++i) {

					DFAgentDescription dfd = results[i];
					AID provider = dfd.getName();
					Iterator it = dfd.getAllServices();

					while (it.hasNext()) {
						ServiceDescription sd = (ServiceDescription) it.next();
						if (sd.getType().equals(serviceType)) {
							System.out.println("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
						}
					}
				}
			}
			else {
				System.out.println("Agent "+getLocalName()+" did not find any "+serviceType+" service");
			}

			System.out.println("Starting Contract with Technicians...");
			this.addBehaviour(new RequestRepair(results));
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}


	//TODO: make public abstract ??
	public boolean compareTechnicianMessages(TechnicianMessage msg1, TechnicianMessage msg2){
		//TODO: Compare technician messages taking into account the client type

		// Return true if msg1 it's better than msg2
		return true;
	}

	public Location getLocation() {
		return location;
	}

	public ClientType getType() {
		return type;
	}
}

