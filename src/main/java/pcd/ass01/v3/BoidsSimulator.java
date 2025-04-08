package pcd.ass01.v3;

import pcd.ass01.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator implements BoidsController {

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private List<Thread> workers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES;
    private long t0;
    private Flag runFlag, resetFlag;

    private volatile MyCyclicBarrier computeVelocityBarrier;
    private volatile MyCyclicBarrier updateVelocityBarrier;
    private volatile MyCyclicBarrier updatePositionBarrier;

    public BoidsSimulator(BoidsModel model, Flag runFlag, Flag resetFlag) {
        this.model = model;
        view = Optional.empty();
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
    }

    private void initWorkers() {
        workers.clear();

        var boids = model.getBoids();
        computeVelocityBarrier = new MyCyclicBarrier(boids.size() + 1, "velocity1");
        updateVelocityBarrier = new MyCyclicBarrier(boids.size(), "velocity2");
        updatePositionBarrier = new MyCyclicBarrier(boids.size() + 1, "position");

        boids.forEach(boid -> {
            Thread t = Thread.ofVirtual().unstarted(new VirtualBoidWorker(boid,
                    model,
                    computeVelocityBarrier,
                    updateVelocityBarrier,
                    updatePositionBarrier,
                    runFlag,
                    resetFlag
            ));
            workers.add(t);
        });

        startWorkers();
    }

    private void startWorkers() {
        workers.forEach(Thread::start);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() {
        initWorkers();
        if (view.isPresent()) {
            runSimulationWithView(view.get());
        } else {
            runFlag.set();
            runSimulationWithoutView();
        }
    }

    private void runSimulationWithView(BoidsView view) {
        while (true) {
            if (runFlag.isSet()) {
                t0 = System.currentTimeMillis();
                computeVelocityBarrier.await();
                updatePositionBarrier.await();
                view.update(framerate, new ArrayList<>(model.getBoids()));
                updateFrameRate(t0);
            }

            if (resetFlag.isSet()) {
                model.resetBoids(view.getNumberOfBoids());
                view.update(framerate, new ArrayList<>(model.getBoids()));
                notifyResetUnpressed();
                initWorkers();
            }
        }
    }

    private void runSimulationWithoutView() {
        while (true) {
            System.out.println("[" + this + "] " + Thread.currentThread().getName() + " -> Running");
            computeVelocityBarrier.await();
            updatePositionBarrier.await();
        }
    }

    public void notifyStart() {
        runFlag.set();
    }

    public void notifyStop() {
        runFlag.reset();
    }

    public void notifyResetPressed() {
        resetFlag.set();
    }

    public void notifyResetUnpressed() {
        resetFlag.reset();
    }

    private void updateFrameRate(long t0) {
        var t1 = System.currentTimeMillis();
        var dtElapsed = t1 - t0;
        var frameratePeriod = 1000 / FRAMERATE;
        if (dtElapsed < frameratePeriod) {
            try {
                Thread.sleep(frameratePeriod - dtElapsed);
            } catch (Exception ex) {
                System.out.println(ex);
            }
            framerate = FRAMERATE;
        } else {
            framerate = (int) (1000 / dtElapsed);
        }
    }
}