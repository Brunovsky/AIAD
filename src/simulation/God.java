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
    final public Lock lock = new ReentrantLock();
    final public Condition day = lock.newCondition();
    final public Condition night = lock.newCondition();

    private static final God god = new God();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int awaitingDay = 0, awaitingNight = 0;
    private ScheduledFuture<?> dayFuture, nightFuture;
    private Condition waiter;

    public static God get() {
        return god;
    }

    public void run(int days, Condition waiter) {
        World.get().currentDay = -1;
        SignalDay day = new SignalDay();
        SignalNight night = new SignalNight();
        this.dayFuture = scheduler.scheduleAtFixedRate(day, 2000, 2000, MILLISECONDS);
        this.nightFuture = scheduler.scheduleAtFixedRate(night, 1000, 2000, MILLISECONDS);
        this.waiter = waiter;
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
                waiter.notify();
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
}
