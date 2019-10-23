package agents;

import agentbehaviours.RequestRepair;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.leap.Iterator;

public class Client extends Agent {

	protected void setup() {
        System.out.println("Setup Client Agent");
		String serviceType = "tech-repairs";

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
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		this.addBehaviour(new RequestRepair());
	}
}

