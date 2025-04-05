package pcd.ass01.v1fix;

import java.util.List;
import java.util.Optional;

public class BoidWorker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;
    private final Optional<BoidsView> view;
    private final MyCyclicBarrierLocks computeVelocityBarrier;
    private final MyCyclicBarrierLocks updateVelocityBarrier;
    private final MyCyclicBarrierLocks updatePositionBarrier;
    private final MyCyclicBarrierLocks updateGuiBarrier;

    public BoidWorker(String name,
                      List<Boid> boidsPartition,
                      BoidsModel model,
                      Optional<BoidsView> view,
                      MyCyclicBarrierLocks computeVelocityBarrier,
                      MyCyclicBarrierLocks updateVelocityBarrier,
                      MyCyclicBarrierLocks updatePositionBarrier,
                      MyCyclicBarrierLocks updateGuiBarrier) {
        super(name);
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.view = view;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.updatePositionBarrier = updatePositionBarrier;
        this.updateGuiBarrier = updateGuiBarrier;
    }

    public void run() {
        if (view.isPresent()) {
            runWithView(view.get());
        } else {
            runWithoutView();
        }
    }

    private void runWithView(BoidsView view) {
        while (!view.isResetButtonPressed()) {
            while (view.isRunning()) {
                computeVelocity();
                updateVelocity();
                updatePosition();
            }
        }
    }

    private void runWithoutView() {
        while (true) {
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
        updateGuiBarrier.await();
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }
}