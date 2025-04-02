package pcd.ass01.v1fix;

public class MyCyclicBarrier {
    private int generation;
    private final int parties;
    private int count;

    public MyCyclicBarrier(int parties) {
        this.parties = parties;
        this.generation = 0;
        this.count = 0;
    }

    public synchronized void await() {
        var currentGeneration = generation;
        count++;

        if (count != parties) {
            while (currentGeneration == generation) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // throw new RuntimeException(e);
                }
            }
        } else {
            count = 0;
            generation++;
            notifyAll();
        }
    }
}