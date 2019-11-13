package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import agents.Client;
import agents.Technician;
import simulation.ClientsDesc.ClientBehaviour;
import simulation.TechniciansDesc.TechnicianBehaviour;

public class Simulation {
    private World world;

    private Map<TechnicianBehaviour, ArrayList<Technician>> technicianMap;
    private ArrayList<Technician> technicianAgents;
    private ArrayList<Client> clientAgents;

    public Simulation(World world) {
        assert world != null;
        this.world = world;

        technicianMap = new HashMap<>();
        technicianAgents = new ArrayList<>();
        clientAgents = new ArrayList<>();
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
    private void launchTechnicians() {
        int[] indices = new int[world.T];
        for (int i = 0; i < world.T; ++i) indices[i] = i;
        shuffle(indices);

        int i = 0;

        for (TechniciansDesc entry : world.technicians) {
            TechnicianBehaviour behaviour = entry.behaviour;
            technicianMap.put(behaviour, new ArrayList<>());

            for (int j = 0; j < entry.number; ++j) {
                double theta = (2.0 * indices[i++]) * Math.PI / (double) world.T;

                double x = world.technicianRadius * Math.cos(theta);
                double y = world.technicianRadius * Math.sin(theta);

                Technician technician = null;
                // ^ Initialize technician: x, y, behaviour

                technicianMap.get(behaviour).add(technician);
                technicianAgents.add(technician);
            }
        }
    }

    /**
     * Launch all world clients according to the world specification with randomly ordered
     * locations.
     */
    private void launchClients() {
        int[][] indices = new int[world.C][2];
        ClientBehaviour behaviours[] = new ClientBehaviour[world.C];
        for (int c = 0, i = 0; c < world.clientRadius.length; ++c) {
            for (int n = 0; n < world.clientNumbers[c]; ++n, ++i) {
                indices[i] = new int[] {c, n};
            }
        }
        int z = 0;
        for (ClientsDesc entry : world.clients) {
            for (int k = 0; k < entry.number; ++k, ++z) {
                behaviours[z] = entry.behaviour;
            }
        }
        shuffle(indices);
        shuffle(behaviours);

        for (int i = 0; i < world.C; ++i) {
            int circle = indices[i][0], k = indices[i][1];
            double radius = world.clientRadius[circle];
            double n = (double) world.clientNumbers[circle];
            double theta = (2.0 * k) * Math.PI / n;

            double time = world.period * i;
            double x = radius * Math.cos(theta);
            double y = radius * Math.sin(theta);
            ClientBehaviour behaviour = behaviours[i];

            Client client = null;
            // ^ Initialize client: x, y, behaviour, time?

            clientAgents.add(client);
        }
    }
}
