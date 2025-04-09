package pcd.ass01.v3;

import pcd.ass01.common.*;

public class VirtualBoidWorker implements Runnable {

    private Boid boid;
    private BoidsModel model;
    private MyCyclicBarrier computeVelocityBarrier;
    private MyCyclicBarrier updateVelocityBarrier;
    private MyCyclicBarrier updatePositionBarrier;
    private Flag runFlag;
    private Flag resetFlag;

    public VirtualBoidWorker(Boid boid,
                             BoidsModel model,
                             MyCyclicBarrier computeVelocityBarrier,
                             MyCyclicBarrier updateVelocityBarrier,
                             MyCyclicBarrier updatePositionBarrier,
                             Flag runFlag,
                             Flag resetFlag) {
        this.boid = boid;
        this.model = model;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.updatePositionBarrier = updatePositionBarrier;
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
    }

    @Override
    public void run() {
        while (!resetFlag.isSet()) {
            while (runFlag.isSet()) {
                boid.calculateVelocity(model);
                if(resetFlag.isSet()) break;
                computeVelocityBarrier.await();

                boid.updateVelocity(model);
                if(resetFlag.isSet()) break;
                updateVelocityBarrier.await();

                boid.updatePosition(model);
                if(resetFlag.isSet()) break;
                updatePositionBarrier.await();
            }
        }
        System.out.println("Terminating");
    }
}