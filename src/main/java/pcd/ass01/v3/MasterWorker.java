package pcd.ass01.v3;

import pcd.ass01.common.*;

import java.util.ArrayList;

public class MasterWorker extends Thread {

    private BoidsModel model;
    private BoidsView boidsView;
    private BoidsSimulator boidsController;
    private MyCyclicBarrier computeVelocityBarrier;
    private MyCyclicBarrier updatePositionBarrier;
    private Flag runFlag;
    private Flag resetFlag;
    private int framerate;

    public MasterWorker(BoidsModel model,
                        BoidsView boidsView,
                        BoidsSimulator boidsController,
                        MyCyclicBarrier computeVelocityBarrier,
                        MyCyclicBarrier updatePositionBarrier,
                        Flag runFlag, Flag resetFlag, int framerate) {
        this.model = model;
        this.boidsView = boidsView;
        this.boidsController = boidsController;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updatePositionBarrier = updatePositionBarrier;
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
        this.framerate = framerate;
    }

    @Override
    public void start() {
        long t0;
        while (true) {
            if (runFlag.isSet()) {
                System.out.println("Running");
                t0 = System.currentTimeMillis();
                computeVelocityBarrier.await();
                updatePositionBarrier.await();
                System.out.println("Waited");
                boidsView.update(framerate, new ArrayList<>(model.getBoids()));
                boidsController.updateFrameRate(t0);
            } else
                System.out.println("Stopped");
        }
    }
}