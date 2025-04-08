package pcd.ass01.common;

import java.util.concurrent.locks.ReentrantLock;

public class Flag {

    private volatile boolean flag;
    private final MyReadWriteLock rwlock;

    public Flag() {
        flag = false;
        this.rwlock = new MyReadWriteLock();
    }

    public void reset() {
        rwlock.writeLock();
        flag = false;
        rwlock.writeUnlock();
    }

    public void set() {
        rwlock.writeLock();
        flag = true;
        rwlock.writeUnlock();
    }

    public boolean isSet() {
        try {
            rwlock.readLock();
            return flag;
        } finally {
            rwlock.readUnlock();
        }
    }
}