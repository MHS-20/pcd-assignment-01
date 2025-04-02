package pcd.ass01.v1fix;

import java.util.List;

public class BoidWorker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;
    private final Monitor monitor;
    private final MyCyclicBarrier computeVelocityBarrier;
    private final MyCyclicBarrier updateVelocityBarrier;
    private final MyCyclicBarrier upddatePositionBarrier;
    private final MyCyclicBarrier updateGuiBarrier;

    public BoidWorker(String name,
                      List<Boid> boidsPartition,
                      BoidsModel model,
                      Monitor monitor,
                      MyCyclicBarrier computeVelocityBarrier,
                      MyCyclicBarrier updateVelocityBarrier,
                      MyCyclicBarrier upddatePositionBarrier,
                      MyCyclicBarrier updateGuiBarrier) {
        super(name);
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.monitor = monitor;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.upddatePositionBarrier = upddatePositionBarrier;
        this.updateGuiBarrier = updateGuiBarrier;
    }

    public void run() {
        while (true) {
            monitor.waitUntilWorkStart();
            computeVelocity();
            updateVelocity();
            updatePosition();
        }
    }

    private void computeVelocity() {
        boidsPartition.forEach(boid -> boid.calculateVelocity(model));
        computeVelocityBarrier.await();
    }

    private void updateVelocity() {
        boidsPartition.forEach(boid -> boid.updateVelocity(model));
        updateVelocityBarrier.await();
    }

    private void updatePosition() {
        boidsPartition.forEach(boid -> boid.updatePosition(model));
        upddatePositionBarrier.await();
        updateGuiBarrier.await();
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }
}