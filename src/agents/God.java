package agents;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class God{

    private double currentDay;

    final public Lock lock = new ReentrantLock();
    final public Condition day = lock.newCondition();
    final public Condition night = lock.newCondition();

    public God() {

    }

    public void startDay(){
        day.signalAll();
    }

    public void awaitEndDay(){

    }

    public void startNight(){

    }

    public void awaitEndNight(){

    }
}
