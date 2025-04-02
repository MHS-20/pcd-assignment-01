package pcd.ass01.v1fix;

import java.util.List;

public class BoidWorker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;
    private final MyCyclicBarrier computeVelocityBarrier;
    private final MyCyclicBarrier updateVelocityBarrier;
    private final MyCyclicBarrier updatePositionBarrier;
    private final MyCyclicBarrier updateGuiBarrier;

    public BoidWorker(String name,
                      List<Boid> boidsPartition,
                      BoidsModel model,
                      MyCyclicBarrier computeVelocityBarrier,
                      MyCyclicBarrier updateVelocityBarrier,
                      MyCyclicBarrier updatePositionBarrier,
                      MyCyclicBarrier updateGuiBarrier) {
        super(name);
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.updatePositionBarrier = updatePositionBarrier;
        this.updateGuiBarrier = updateGuiBarrier;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            computeVelocity();
            updateVelocity();
            updatePosition();
        }
        System.out.println("Exiting");
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
        updatePositionBarrier.await();
        updateGuiBarrier.await();
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }
}