package pcd.ass01.common;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyCyclicBarrier {
    private volatile int generation;
    private final int parties;
    private volatile int count;
    private ReentrantLock mutex;
    private Condition cond;
    private String name;
    private volatile boolean broken = false;

    public MyCyclicBarrier(int parties) {
        this.parties = parties;
        this.generation = 0;
        this.count = 0;
        this.mutex = new ReentrantLock();
        this.cond = mutex.newCondition();
    }

    public MyCyclicBarrier(int parties, String name) {
        this(parties);
        this.name = name;
        this.mutex = new ReentrantLock() {
            public String toString() { return name; }
        };
        this.cond = mutex.newCondition();
    }

    public void await() {
        mutex.lock();

        var currentGeneration = generation;
        count++;
        if (count != parties) {
            while (currentGeneration == generation && !broken) {
                try {
                    //System.out.println("Thread " + Thread.currentThread() + " is waiting on barrier " + name + " with count: " + count);
                    cond.await();
                } catch (InterruptedException e) {
                   throw new RuntimeException(e);
                }
            }
        } else {
            //System.out.println("Thread " + Thread.currentThread() + " is releasing barrier " + name);
            count = 0;
            generation++;
            broken = false;
            cond.signalAll();
        }
        mutex.unlock();
    }

    public void breaks(){
        mutex.lock();
        count = 0;
        generation++;
        broken = true;
        cond.signalAll();
        mutex.unlock();
    }

    public void resets() {
        mutex.lock();
        count = 0;
        generation = 0;
        broken = false;
        cond.signalAll();
        mutex.unlock();
    }
}
