package simulation;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import utils.Logger;

public class God extends Agent {
    private static final long serialVersionUID = -2050222210117180499L;

    public final Lock lock = new ReentrantLock();
    public final Set<AID> day = new HashSet<>();
    public final Set<AID> night = new HashSet<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> dayFuture, nightFuture;
    private final Lock simLock = new ReentrantLock();
    private final Condition simWaiter = simLock.newCondition();

    private static God god;

    public static void renew() {
        god = new God();
    }

    public static God get() {
        return god;
    }

    public void runSimulation() {
        World.get().currentDay = -1;
        int period = World.MILLI_PERIOD;
        int delay = World.MILLI_DELAY;
        SignalDay day = new SignalDay();
        SignalNight night = new SignalNight();
        dayFuture = scheduler.scheduleAtFixedRate(day, delay + period / 2, period, MILLISECONDS);
        nightFuture = scheduler.scheduleAtFixedRate(night, delay, period, MILLISECONDS);
    }

    public void awaitWorld() {
        try {
            simLock.lock();
            simWaiter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(2);
        } finally {
            simLock.unlock();
        }
    }

    public void awaitNight(AID me) {
        try {
            lock.lock();
            night.add(me);
        } finally {
            lock.unlock();
        }
    }

    public void awaitDay(AID me) {
        try {
            lock.lock();
            day.add(me);
        } finally {
            lock.unlock();
        }
    }

    public void wakeup(Set<AID> sleeping, String ontology) {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        for (AID agent : sleeping) message.addReceiver(agent);
        message.setOntology(ontology);
        send(message);
    }

    private class SignalNight implements Runnable {
        @Override
        public void run() {
            int total = World.get().getTotalAgents();
            if (++World.get().currentDay == World.get().numberDays) {
                nightFuture.cancel(false);
            }

            try {
                lock.lock();
                String text = String.format("%d/%d waiting for night", night.size(), total);
                if (Simulation.SIMULATION_DEBUG_MODE) Logger.god(text);
                Logger.god("Night " + World.get().currentDay);
                wakeup(night, "simulation-night");
            } finally {
                night.clear();
                lock.unlock();
            }
        }
    }

    private class SignalDay implements Runnable {
        @Override
        public void run() {
            int total = World.get().getTotalAgents();
            if (World.get().currentDay == World.get().numberDays) {
                dayFuture.cancel(false);
                scheduler.schedule(new Terminate(), World.MILLI_PERIOD / 2, MILLISECONDS);
            }

            try {
                lock.lock();
                String text = String.format("%d/%d waiting for day", night.size(), total);
                if (Simulation.SIMULATION_DEBUG_MODE) Logger.god(text);
                Logger.god("Day   " + World.get().currentDay);
                wakeup(day, "simulation-day");
            } finally {
                day.clear();
                lock.unlock();
            }
        }
    }

    private class Terminate implements Runnable {
        @Override
        public void run() {
            try {
                simLock.lock();
                simWaiter.signalAll();
            } finally {
                simLock.unlock();
            }
        }
    }
}
