package pcd.ass01.common;

import java.util.ArrayList;

public class MasterAgent extends Thread {

    private BoidsModel model;
    private BoidsView boidsView;
    private BoidsController controller;
    private MyCyclicBarrier computeVelocityBarrier;
    private MyCyclicBarrier updatePositionBarrier;
    private Flag runFlag;
    private Flag resetFlag;
    private int framerate;

    public MasterAgent(BoidsModel model,
                       BoidsView boidsView,
                       BoidsController controller,
                       MyCyclicBarrier computeVelocityBarrier,
                       MyCyclicBarrier updatePositionBarrier,
                       Flag runFlag, Flag resetFlag, int framerate) {
        this.model = model;
        this.boidsView = boidsView;
        this.controller = controller;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updatePositionBarrier = updatePositionBarrier;
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
        this.framerate = framerate;
    }

    @Override
    public void run() {
        long t0;
        while (!resetFlag.isSet()) {
            if (runFlag.isSet()) {
                t0 = System.currentTimeMillis();
                computeVelocityBarrier.await();
                updatePositionBarrier.await();
                boidsView.update(framerate, new ArrayList<>(model.getBoids()));
                framerate = controller.updateFrameRate(t0);
            }
        }
    }
}