package pcd.ass01.v3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyCyclicBarrier {
    private int generation;
    private final int parties;
    private int count;
    private ReentrantLock mutex;
    private Condition cond;

    public MyCyclicBarrier(int parties) {
        this.parties = parties;
        this.generation = 0;
        this.count = 0;
        this.mutex = new ReentrantLock();
        this.cond = mutex.newCondition();
    }

    public void await() {
        mutex.lock();

        var currentGeneration = generation;
        count++;
        if (count != parties) {
            while (currentGeneration == generation) {
                try {
                    cond.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } else {
            count = 0;
            generation++;
            cond.signalAll();
        }

        mutex.unlock();
    }
}
