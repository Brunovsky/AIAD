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
import agents.Company;
import agents.Station;
import agents.Technician;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import strategies.ClientStrategy;
import strategies.TechnicianStrategy;
import strategies.company.CompanyStrategy;
import utils.MalfunctionType;

public class Simulation {

    private ArrayList<Technician> technicianAgents;
    private ArrayList<Client> clientAgents;
    private ArrayList<Station> stationAgents;
    private ArrayList<Company> companyAgents;

    private Map<ClientStrategy, ArrayList<Technician>> clientMap;
    private Map<CompanyStrategy, ArrayList<Technician>> companyMap;
    private Map<TechnicianStrategy, ArrayList<Technician>> technicianMap;

    private Runtime runtime;
    private Profile profile;
    private ContainerController container;

    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        // World.set(new PortoWorld());
        new Simulation();
    }

    public Simulation() {
        technicianAgents = new ArrayList<>();
        clientAgents = new ArrayList<>();
        stationAgents = new ArrayList<>();
        companyAgents = new ArrayList<>();

        runtime = Runtime.instance();
        profile = new ProfileImpl(true);
        container = runtime.createAgentContainer(profile);

        launchStations();
        launchClients();
        launchTechnicians();
        launchCompanies();
        printStatistics();

        try {
            for (Technician technician : technicianAgents) technician.doDelete();
            for (Company company : companyAgents) company.doDelete();
            for (Client client : clientAgents) client.doDelete();
            for (Station station : stationAgents) station.doDelete();
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
    private void fillArrays(int[] indices, ClientStrategy[] strategies) {
        for (int n = 0; n < World.get().clientNumbers[c]; ++n) {
            indices[i] = new int[] {c, n};
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
     * Launch all world stations
     */
    private boolean launchStations() {

        int[] indices = new int[World.get().S];
        int i = 0;

        for (i = 0; i < World.get().S; ++i) {
            String id = "station_" + (i + 1);

            Station station = new Station(id);

            try {
                AgentController ac = container.acceptNewAgent(id, station);
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
                return false;
            }

            stationAgents.add(station);
        }
        return false;
    }

    /**
     * Launch all world clients according to the world specification with randomly ordered
     * locations.
     */
    private boolean launchClients() {
        int[] indices = new int[World.get().Cl];
        ClientStrategy strategies[] = new ClientStrategy[World.get().Cl];
        AID[] stations = new AID[World.get().Cl];

        for (int n = 0; n < World.get().Cl; ++n) {
            indices[n] = n;
        }

        int z = 0;
        for (ClientsDesc entry : World.get().clients) {
            for (int k = 0; k < entry.number; ++k, ++z) {
                strategies[z] = entry.strategy;
            }
        }
        
        z = 0;
        for (StationsDesc entry : World.get().stations) {
            for (int k = 0; k < entry.number; ++k, ++z) {
                stations[z] = entry.strategy;
            }
        }
        
        //fillArrays(indices, personalities, types);
        shuffle(indices);
        shuffle(strategies);

        for (int i = 0; i < World.get().Cl; ++i) {

            String id = "client_" + (i + 1);

            ClientStrategy strategy = strategies[i];
            
            Client client = new Client(id, strategy, station );

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

    /**
     * Launch all world companies
     */
    private boolean launchCompanies() {
        return false;
    }

    private boolean launchTechnicians() {
        int[] indices = new int[World.get().T];
        for (int i = 0; i < World.get().T; ++i) indices[i] = i;
        shuffle(indices);

        int i = 0;

        for (TechniciansDesc entry : World.get().technicians) {
            TechnicianStrategy personality = entry.personality;
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
