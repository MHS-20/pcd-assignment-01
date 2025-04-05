package pcd.ass01.common;

import java.util.concurrent.locks.ReentrantLock;

public class Flag {

    private boolean flag;
    private final ReentrantLock lock;

    public Flag() {
        flag = false;
        this.lock = new ReentrantLock();
    }

    public void reset() {
        lock.lock();
        flag = false;
        lock.unlock();
    }

    public void set() {
        lock.lock();
        flag = true;
        lock.unlock();
    }

    public boolean isSet() {
        try {
            lock.lock();
            return flag;
        } finally {
            lock.unlock();
        }
    }
}