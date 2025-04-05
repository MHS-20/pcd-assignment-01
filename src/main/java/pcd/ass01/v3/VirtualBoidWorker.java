package pcd.ass01.v3;

import pcd.ass01.common.*;

public class VirtualBoidWorker implements Runnable {

    private Boid boid;
    private BoidsModel model;
    private MyCyclicBarrier computeVelocityBarrier;
    private MyCyclicBarrier updateVelocityBarrier;
    private MyCyclicBarrier updatePositionBarrier;
    private MyCyclicBarrier updateGuiBarrier;
    private Flag runFlag;
    private Flag resetFlag;

    public VirtualBoidWorker(Boid boid,
                             BoidsModel model,
                             MyCyclicBarrier computeVelocityBarrier,
                             MyCyclicBarrier updateVelocityBarrier,
                             MyCyclicBarrier updatePositionBarrier,
                             MyCyclicBarrier updateGuiBarrier,
                             Flag runFlag,
                             Flag resetFlag) {
        this.boid = boid;
        this.model = model;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.updatePositionBarrier = updatePositionBarrier;
        this.updateGuiBarrier = updateGuiBarrier;
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
    }

    @Override
    public void run() {
        while (!resetFlag.isSet()) {
            while (runFlag.isSet()) {
                boid.calculateVelocity(model);
                computeVelocityBarrier.await();

                boid.updateVelocity(model);
                updateVelocityBarrier.await();

                boid.updatePosition(model);
                updatePositionBarrier.await();
                updateGuiBarrier.await();
            }
        }
    }
}