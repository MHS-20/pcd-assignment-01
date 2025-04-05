package pcd.ass01.common;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MyFIFOqueue<T> implements FIFOqueue<T> {
    private List<T> queue;
    private final ReentrantLock lock;

    public MyFIFOqueue() {
        this.queue = new LinkedList<T>();
        this.lock = new ReentrantLock();
    }

    public MyFIFOqueue(List<T> queue) {
        this.queue = queue;
        this.lock = new ReentrantLock();
    }

    @Override
    public void put(T item) {
        lock.lock();
        queue.add(item);
        lock.unlock();
    }

    @Override
    public T get() {
        try {
            lock.lock();
            if (queue.isEmpty())
                return null;
            else
                return queue.removeFirst();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            lock.lock();
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.lock();
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        try {
            lock.lock();
            queue.clear();
        } finally {
            lock.unlock();
        }
    }
}
