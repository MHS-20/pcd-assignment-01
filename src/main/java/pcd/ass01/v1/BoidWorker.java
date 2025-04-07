package pcd.ass01.v1;

import pcd.ass01.common.*;

import java.util.List;
import java.util.Optional;

public class BoidWorker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;
    private final Optional<BoidsView> view;

    private final MyCyclicBarrier computeVelocityBarrier;
    private final MyCyclicBarrier updateVelocityBarrier;
    private final MyCyclicBarrier updatePositionBarrier;
    private final MyCyclicBarrier updateGuiBarrier;

    private Flag runFlag, resetFlag;


    public BoidWorker(String name,
                      List<Boid> boidsPartition,
                      BoidsModel model,
                      Optional<BoidsView> view,
                      Flag runFlag, Flag resetFlag,
                      MyCyclicBarrier computeVelocityBarrier,
                      MyCyclicBarrier updateVelocityBarrier,
                      MyCyclicBarrier updatePositionBarrier,
                      MyCyclicBarrier updateGuiBarrier) {
        super(name);
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.view = view;
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.updatePositionBarrier = updatePositionBarrier;
        this.updateGuiBarrier = updateGuiBarrier;
    }

    public void run() {
        while (!resetFlag.isSet()) {
            while (runFlag.isSet()) {
                computeVelocity();
                updateVelocity();
                updatePosition();
            }
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
        //updateGuiBarrier.await();
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }
}