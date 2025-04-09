package pcd.ass01.v1;

import pcd.ass01.common.*;

import java.util.List;
import java.util.Optional;

public class BoidWorker extends Thread {

    private final List<Boid> boidsPartition;
    private final BoidsModel model;

    private final MyCyclicBarrier computeVelocityBarrier;
    private final MyCyclicBarrier updateVelocityBarrier;
    private final MyCyclicBarrier updatePositionBarrier;

    private final Flag runFlag;
    private final Flag resetFlag;

    public BoidWorker(String name,
                      List<Boid> boidsPartition,
                      BoidsModel model,
                      Flag runFlag, Flag resetFlag,
                      MyCyclicBarrier computeVelocityBarrier,
                      MyCyclicBarrier updateVelocityBarrier,
                      MyCyclicBarrier updatePositionBarrier) {
        super(name);
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.updatePositionBarrier = updatePositionBarrier;
    }

    public void run() {
        while (!resetFlag.isSet()) {
            while (runFlag.isSet()) {
                boidsPartition.forEach(boid -> boid.calculateVelocity(model));
                computeVelocityBarrier.await();

                boidsPartition.forEach(boid -> boid.updateVelocity(model));
                updateVelocityBarrier.await();

                boidsPartition.forEach(boid -> boid.updatePosition(model));
                updatePositionBarrier.await();
            }
        }
    }

    private void log(String msg) {
        synchronized (System.out) {
            System.out.println("[" + this + "] " + getName() + " -> " + msg);
        }
    }
}