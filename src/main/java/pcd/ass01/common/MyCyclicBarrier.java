package pcd.ass01.common;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyCyclicBarrier {
    private int generation;
    private final int parties;
    private int count;
    private ReentrantLock mutex;
    private Condition cond;
    private String name;

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
            while (currentGeneration == generation) {
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
            cond.signalAll();
        }
        mutex.unlock();
    }
}
