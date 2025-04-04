package pcd.ass01.measurements.v1;

import java.util.List;

public class BoidWorker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;
    private final MyCyclicBarrierLocks computeVelocityBarrier;
    private final MyCyclicBarrierLocks updateVelocityBarrier;
    private final MyCyclicBarrierLocks updatePositionBarrier;

    public BoidWorker(String name,
                      List<Boid> boidsPartition,
                      BoidsModel model,
                      MyCyclicBarrierLocks computeVelocityBarrier,
                      MyCyclicBarrierLocks updateVelocityBarrier,
                      MyCyclicBarrierLocks updatePositionBarrier) {
        super(name);
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.updatePositionBarrier = updatePositionBarrier;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
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
        updatePositionBarrier.await();
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }
}