package pcd.ass01.v3;

import pcd.ass01.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator implements BoidsController {

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private List<Thread> workers = new ArrayList<>();

    private static final int MAX_FRAMERATE = 50;
    private int framerate;
    private Flag runFlag, resetFlag;
    private MasterAgent master;

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
        computeVelocityBarrier = new MyCyclicBarrier(boids.size() + 1);
        updateVelocityBarrier = new MyCyclicBarrier(boids.size());
        updatePositionBarrier = new MyCyclicBarrier(boids.size() + 1);
        master = new MasterAgent(
                model,
                view.get(),
                this,
                computeVelocityBarrier,
                updatePositionBarrier,
                runFlag,
                resetFlag,
                MAX_FRAMERATE
        );

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
            master.start();
            runSimulationWithView(view.get());
        } else {
            runFlag.set();
            runSimulationWithoutView();
        }
    }

    private void runSimulationWithView(BoidsView view) {
        while (true) {
            if (resetFlag.isSet()) {
                terminateWorkers();
                model.resetBoids(view.getNumberOfBoids());
                view.update(framerate, new ArrayList<>(model.getBoids()));
                notifyResetUnpressed();
                initWorkers();
                master.start();
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

    private void terminateWorkers() {
        // awakes suspended threads if any
        computeVelocityBarrier.breaks();
        updateVelocityBarrier.breaks();
        updatePositionBarrier.breaks();

        try {
            //checks all threads have terminated
            for (Thread w : workers)
                w.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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

    public int updateFrameRate(long t0) {
        var t1 = System.currentTimeMillis();
        var dtElapsed = t1 - t0;
        var frameratePeriod = 1000 / MAX_FRAMERATE;
        if (dtElapsed < frameratePeriod) {
            try {
                Thread.sleep(frameratePeriod - dtElapsed);
            } catch (Exception ex) {
                System.out.println(ex);
            }
            framerate = MAX_FRAMERATE;
        } else {
            framerate = (int) (1000 / dtElapsed);
        }

        return framerate;
    }
}