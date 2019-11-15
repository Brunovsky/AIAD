package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import utils.MalfunctionType;
import utils.TechnicianType;
import utils.TimeBoard;

public class Simulation {
    private Map<TechnicianType, ArrayList<Technician>> technicianMap;
    private ArrayList<Technician> technicianAgents;
    private ArrayList<Client> clientAgents;

    private Runtime runtime;
    private Profile profile;
    private ContainerController container;

    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        World.set(new PortoWorld());
        new Simulation();
    }

    public Simulation() {
        technicianMap = new HashMap<>();
        technicianAgents = new ArrayList<>();
        clientAgents = new ArrayList<>();

        runtime = Runtime.instance();
        profile = new ProfileImpl(true);
        container = runtime.createAgentContainer(profile);

        launchTechnicians();
        launchClients();
        printStatistics();

        try {
            for (Technician technician : technicianAgents) technician.doDelete();
            container.kill();
            runtime.shutDown();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private double findEndTime() {
        double endTime = 0;
        for (Technician technician : technicianAgents) {
            double tempEndTime = technician.getTimeBoard().getLastSlotEndTime();
            if (endTime < tempEndTime) {
                endTime = tempEndTime;
            }
        }
        return endTime;
    }

    private void printStatistics() {  // get last technician endtime
        double endTime = findEndTime();

        for (Technician technician : technicianAgents) {
            TimeBoard timeBoard = technician.getTimeBoard();

            System.out.println("> Technician " + technician.getLocalName());
            System.out.println("- Number of slots: " + timeBoard.getNumberOfTimeSlots());
            System.out.println("- Receipts: " + timeBoard.getReceipts() + "€");
            System.out.println("- Occupied time: " + timeBoard.getOccupiedTime());
            System.out.println("- Travel time: " + timeBoard.getTravelTime());
            System.out.println("- Work time: " + timeBoard.getWorkTime());
            System.out.println("- Working %: " + timeBoard.getWorkTime() / endTime * 100 + "%");

            System.out.println("--------------------------");
        }
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

    // fill the arrays to be shuffled with the appropriate frequencies
    private void fillArrays(int[][] indices, ClientType[] personalities, MalfunctionType[] types) {
        for (int c = 0, i = 0; c < World.get().clientRadius.length; ++c) {
            for (int n = 0; n < World.get().clientNumbers[c]; ++n, ++i) {
                indices[i] = new int[] {c, n};
            }
        }

        int z = 0;
        for (ClientsDesc entry : World.get().clients) {
            for (int k = 0; k < entry.number; ++k, ++z) {
                personalities[z] = entry.personality;
            }
        }

        z = 0;
        final int[] malfunctions = World.get().malfunctions;
        for (int i = 0; i < malfunctions[0]; ++i, ++z) types[z] = MalfunctionType.HARD;
        for (int i = 0; i < malfunctions[1]; ++i, ++z) types[z] = MalfunctionType.MEDIUM;
        for (int i = 0; i < malfunctions[2]; ++i, ++z) types[z] = MalfunctionType.EASY;
    }

    /**
     * Launch all world technicians
     */
    private boolean launchTechnicians() {
        int[] indices = new int[World.get().T];
        for (int i = 0; i < World.get().T; ++i) indices[i] = i;
        shuffle(indices);

        int i = 0;

        for (TechniciansDesc entry : World.get().technicians) {
            TechnicianType personality = entry.personality;
            technicianMap.putIfAbsent(personality, new ArrayList<>());

            for (int j = 0; j < entry.number; ++j, ++i) {
                double theta = (2.0 * indices[i]) * Math.PI / (double) World.get().T;

                String id = "technician_" + (i + 1);
                double x = World.get().technicianRadius * Math.cos(theta);
                double y = World.get().technicianRadius * Math.sin(theta);
                Location location = new Location(x, y);

                Technician technician = new Technician(location, personality);

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
        int[][] indices = new int[World.get().C][2];
        ClientType personalities[] = new ClientType[World.get().C];
        MalfunctionType types[] = new MalfunctionType[World.get().C];

        fillArrays(indices, personalities, types);
        shuffle(indices);
        shuffle(personalities);
        shuffle(types);

        ClientWaiter waiter = new ClientWaiter();

        for (int i = 0; i < World.get().C; ++i) {
            int circle = indices[i][0], k = indices[i][1];
            double radius = World.get().clientRadius[circle];
            double n = (double) World.get().clientNumbers[circle];
            double theta = (2.0 * k) * Math.PI / n;

            String id = "client_" + (i + 1);
            double x = radius * Math.cos(theta);
            double y = radius * Math.sin(theta);
            Location location = new Location(x, y);
            ClientType personality = personalities[i];
            double time = World.get().period * i;
            MalfunctionType malfunction = types[i];

            Client client = new Client(location, malfunction, time, personality, waiter);

            try {
                AgentController ac = container.acceptNewAgent(id, client);
                ac.start();
                waiter.await();
            } catch (StaleProxyException e) {
                e.printStackTrace();
                return false;
            }

            System.out.println("\n=== === === === === === === === === === ===\n");

            clientAgents.add(client);
        }

        return true;
    }

    public class ClientWaiter implements Client.Callback {
        private Lock lock;
        private Condition condition;

        private ClientWaiter() {
            lock = new ReentrantLock();
            condition = lock.newCondition();
        }

        private boolean await() {
            try {
                lock.lock();
                condition.await();
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } finally {
                lock.unlock();
            }
        }

        private void signal() {
            try {
                lock.lock();
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

        public void run() {
            signal();
        }
    }
}
