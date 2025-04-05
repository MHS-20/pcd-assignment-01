package pcd.ass01.common;

public interface FIFOqueue<T> {

    void put(T item);

    T get();

    boolean isEmpty();

    int size();

    void clear();
}
