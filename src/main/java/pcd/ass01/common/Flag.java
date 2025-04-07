package pcd.ass01.common;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Flag {

    private boolean flag;
    private final ReentrantLock lock;
    private final ReadWriteLock rwlock;


    public Flag() {
        flag = false;
        this.lock = new ReentrantLock();
        this.rwlock = new ReentrantReadWriteLock(true);
    }

    public void reset() {
        rwlock.writeLock().lock();
        flag = false;
        rwlock.writeLock().unlock();
    }

    public void set() {
        rwlock.writeLock().lock();
        flag = true;
        rwlock.writeLock().unlock();
    }

    public boolean isSet() {
        try {
            rwlock.readLock().lock();
            return flag;
        } finally {
            rwlock.readLock().unlock();
        }
    }
}