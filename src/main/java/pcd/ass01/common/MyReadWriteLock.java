package pcd.ass01.common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

public class MyReadWriteLock {
    private final Lock lock;
    private final Condition canRead;
    private final Condition canWrite;

    private volatile int readers = 0;
    private volatile int writers = 0;
    private volatile int writeRequests = 0;

    public MyReadWriteLock() {
        lock = new ReentrantLock();
        canRead = lock.newCondition();
        canWrite = lock.newCondition();
    }

    public void readLock() {
        lock.lock();
        while(writers > 0 || writeRequests > 0) {
            try {
                canRead.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        readers++;
        lock.unlock();
    }

    public void readUnlock(){
        lock.lock();
        readers--;
        if(readers == 0)
            canWrite.signal();
        lock.unlock();
    }

    public void writeLock(){
        lock.lock();
        writeRequests++;

        while(readers > 0){
            try {
                canWrite.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        writers++;
        writeRequests--;
        lock.unlock();
    }

    public void writeUnlock(){
        lock.lock();
        writers--;
        canRead.signalAll();
        lock.unlock();
    }
}