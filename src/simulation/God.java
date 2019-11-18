package simulation;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import utils.Logger;

public class God {
    public final Lock lock = new ReentrantLock();
    public final Condition day = lock.newCondition();
    public final Condition night = lock.newCondition();

    private static final God god = new God();
    private static final int PERIOD = 2000, SETUP = 5000;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int awaitingDay = 0, awaitingNight = 0;
    private ScheduledFuture<?> dayFuture, nightFuture;
    private final Lock simLock = new ReentrantLock();
    private final Condition simWaiter = simLock.newCondition();

    public static God get() {
        return god;
    }

    public void run() {
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

    public void awaitNight() {
        try {
            lock.lock();
            ++awaitingNight;
            night.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            lock.unlock();
        }
    }

    public void awaitDay() {
        try {
            lock.lock();
            ++awaitingDay;
            day.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            lock.unlock();
        }
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
                if (awaitingNight < total) {
                    Logger.warn("GOD", "Only " + awaitingNight + " agents (out of " + total
                                           + ") waiting for night");
                }
                night.signalAll();
            } finally {
                awaitingNight = 0;
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
                if (awaitingDay < total) {
                    Logger.warn("GOD", "Only " + awaitingDay + " agents (out of " + total
                                           + ") waiting for day");
                }
                night.signalAll();
            } finally {
                awaitingDay = 0;
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
