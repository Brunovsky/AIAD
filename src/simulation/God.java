package simulation;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import utils.Logger;

public class God extends Agent {
    public final Lock lock = new ReentrantLock();
    public final Set<AID> day = new HashSet<>();
    public final Set<AID> night = new HashSet<>();

    private static final God god = new God();
    private static final int PERIOD = 5000, SETUP = 150000;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> dayFuture, nightFuture;
    private final Lock simLock = new ReentrantLock();
    private final Condition simWaiter = simLock.newCondition();

    public static God get() {
        return god;
    }

    public void runSimulation() {
        World.get().currentDay = -1;
        SignalDay day = new SignalDay();
        SignalNight night = new SignalNight();
        dayFuture = scheduler.scheduleAtFixedRate(day, SETUP + PERIOD / 2, PERIOD, MILLISECONDS);
        nightFuture = scheduler.scheduleAtFixedRate(night, SETUP, PERIOD, MILLISECONDS);
    }

    public void awaitWorld() {
        try {
            simLock.lock();
            simWaiter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(2);
        } finally {
            lock.unlock();
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
        ACLMessage message = new ACLMessage(ACLMessage.INFORM_REF);
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
                if (night.size() < total) {
                    Logger.warn("GOD", "Only " + night.size() + " agents (out of " + total
                                           + ") waiting for night");
                }
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
                scheduler.schedule(new Terminate(), PERIOD / 2, MILLISECONDS);
            }

            try {
                lock.lock();
                if (day.size() < total) {
                    Logger.warn("GOD", "Only " + day.size() + " agents (out of " + total
                                           + ") waiting for day");
                }
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
                simWaiter.notifyAll();
            } finally {
                simLock.unlock();
            }
        }
    }
}
