package pcd.ass01.v1fix;

public class Monitor {
    private boolean working = false;

    public synchronized void waitUntilWorkStart() {
        while (!working) {
            try {
                wait();
            } catch (InterruptedException e) {
                // throw new RuntimeException(e);
            }
        }
    }

    public synchronized void startWork() {
        if (!working) {
            working = true;
            notifyAll();
        }
    }

    public synchronized void stopWork() {
        if (working) {
            working = false;
        }
    }
}