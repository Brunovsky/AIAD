package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import agents.Client;
import agents.Technician;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utils.ClientType;
import utils.Location;
import utils.TechnicianType;

public class Simulation {
    private World world;

    private Map<TechnicianType, ArrayList<Technician>> technicianMap;
    private ArrayList<Technician> technicianAgents;
    private ArrayList<Client> clientAgents;

    private Runtime runtime;
    private Profile profile;
    private ContainerController container;

    public Simulation(World world) {
        assert world != null;
        this.world = world;

        technicianMap = new HashMap<>();
        technicianAgents = new ArrayList<>();
        clientAgents = new ArrayList<>();

        runtime = Runtime.instance();
        profile = new ProfileImpl(true);
        container = runtime.createMainContainer(profile);
    }

    // shuffle array of ints
    private void shuffle(int[] array) {
        final Random rng = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; --i) {
            int j = rng.nextInt(i + 1);
            int a = array[j];
            array[j] = array[i];
            array[i] = a;
        }
    }

    // shuffle array of objects
    private void shuffle(Object[] array) {
        final Random rng = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; --i) {
            int j = rng.nextInt(i + 1);
            Object a = array[j];
            array[j] = array[i];
            array[i] = a;
        }
    }

    /**
     * Launch all world technicians
     */
    private boolean launchTechnicians() {
        int[] indices = new int[world.T];
        for (int i = 0; i < world.T; ++i) indices[i] = i;
        shuffle(indices);

        int i = 0;

        for (TechniciansDesc entry : world.technicians) {
            TechnicianType personality = entry.personality;
            technicianMap.put(personality, new ArrayList<>());

            for (int j = 0; j < entry.number; ++j) {
                double theta = (2.0 * indices[i++]) * Math.PI / (double) world.T;

                String id = "technician_" + i;
                double x = world.technicianRadius * Math.cos(theta);
                double y = world.technicianRadius * Math.sin(theta);
                Location location = new Location(x, y);

                Technician technician = null;
                // ^ Initialize technician: location, personality

                try {
                    AgentController ac = container.acceptNewAgent(id, technician);
                    ac.start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                    return false;
                }

                technicianMap.get(personality).add(technician);
                technicianAgents.add(technician);
            }
        }

        return true;
    }

    /**
     * Launch all world clients according to the world specification with randomly ordered
     * locations.
     */
    private boolean launchClients() {
        int[][] indices = new int[world.C][2];
        ClientType personalities[] = new ClientType[world.C];
        for (int c = 0, i = 0; c < world.clientRadius.length; ++c) {
            for (int n = 0; n < world.clientNumbers[c]; ++n, ++i) {
                indices[i] = new int[] {c, n};
            }
        }
        int z = 0;
        for (ClientsDesc entry : world.clients) {
            for (int k = 0; k < entry.number; ++k, ++z) {
                personalities[z] = entry.personality;
            }
        }
        shuffle(indices);
        shuffle(personalities);

        for (int i = 0; i < world.C; ++i) {
            int circle = indices[i][0], k = indices[i][1];
            double radius = world.clientRadius[circle];
            double n = (double) world.clientNumbers[circle];
            double theta = (2.0 * k) * Math.PI / n;

            String id = "client_" + i;
            double time = world.period * i;
            double x = radius * Math.cos(theta);
            double y = radius * Math.sin(theta);
            Location location = new Location(x, y);
            ClientType personality = personalities[i];

            Client client = null;
            // ^ Initialize client: id, location, personality, time, malfunction type

            try {
                AgentController ac = container.acceptNewAgent(id, client);
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
                return false;
            }

            clientAgents.add(client);
        }

        return true;
    }
}
