package pcd.ass01.common;

import java.util.concurrent.locks.ReentrantLock;

public class Flag {

    private volatile boolean flag;
    private final ReentrantLock lock;
    //private final ReadWriteLock rwlock;
    private final MyReadWriteLock rwlock;
    //private volatile boolean atomicCheck;

    public Flag() {
        flag = false;
        this.lock = new ReentrantLock();
        //this.rwlock = new ReentrantReadWriteLock(true);
        this.rwlock = new MyReadWriteLock();
    }

    public void reset() {
        //rwlock.writeLock().lock();
        rwlock.writeLock();
        flag = false;
        rwlock.writeUnlock();
        //rwlock.writeLock().unlock();
    }

    public void set() {
       // rwlock.writeLock().lock();
        rwlock.writeLock();
        flag = true;
        rwlock.writeUnlock();
        // rwlock.writeLock().unlock();
    }

    public boolean isSet() {
        try {
            // rwlock.readLock().lock();
            rwlock.readLock();
            return flag;
        } finally {
            rwlock.readUnlock();
            // rwlock.readLock().unlock();
        }
    }
}