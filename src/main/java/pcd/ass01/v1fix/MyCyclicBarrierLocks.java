package pcd.ass01.v1fix;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyCyclicBarrierLocks {
    private int generation;
    private final int parties;
    private int count;
    private ReentrantLock mutex;
    private Condition cond;
    private String name;

    public MyCyclicBarrierLocks(int parties) {
        this.parties = parties;
        this.generation = 0;
        this.count = 0;
        this.mutex = new ReentrantLock();
        this.cond = mutex.newCondition();
    }

    public MyCyclicBarrierLocks(int parties, String name) {
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
            while (currentGeneration == generation) { //&& !Thread.currentThread().isInterrupted()
                try {
                    // System.out.println(Thread.currentThread() + " waiting on " + name + " with count " + count + " for generation: " + currentGeneration);
                    cond.await();
                } catch (InterruptedException e) {
                    //Thread.currentThread().interrupt();
                    //System.out.println("mutex " + mutex.isHeldByCurrentThread());
                    //mutex.unlock();
                    //System.out.println(Thread.currentThread() +  " got interrupted on " + name + " for generation: " + currentGeneration);
                    //return;
                }
            }
        } else {
            //System.out.println(Thread.currentThread() + " " + name + " barrier released for generation: " + currentGeneration + " with count: " + count);
            count = 0;
            generation++;
            cond.signalAll();
        }
        mutex.unlock();
    }
}
