package pcd.ass01.v1;

import pcd.ass01.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator implements BoidsController {

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private final List<BoidWorker> boidWorkers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES;
    private long t0;

    private Flag runFlag, resetFlag;
    private MyCyclicBarrier computeVelocityBarrier;
    private MyCyclicBarrier updateVelocityBarrier;
    private MyCyclicBarrier updatePositionBarrier;

    public BoidsSimulator(BoidsModel model, Flag runFlag, Flag resetFlag) {
        this.model = model;
        view = Optional.empty();
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
    }

    private void initWorkers() {
        boidWorkers.clear();

        List<List<Boid>> partitions = new ArrayList<>();
        for (int i = 0; i < N_WORKERS; i++) {
            partitions.add(new ArrayList<>());
        }

        int i = 0;
        for (Boid boid : model.getBoids()) {
            i = (i == partitions.size() ? 0 : i);
            partitions.get(i).add(boid);
            i++;
        }

        computeVelocityBarrier = new MyCyclicBarrier(N_WORKERS + 1);
        updateVelocityBarrier = new MyCyclicBarrier(N_WORKERS);
        updatePositionBarrier = new MyCyclicBarrier(N_WORKERS + 1);

        i = 0;
        for (List<Boid> partition : partitions) {
            boidWorkers.add(new BoidWorker("W" + i,
                    partition,
                    model,
                    runFlag,
                    resetFlag,
                    computeVelocityBarrier,
                    updateVelocityBarrier,
                    updatePositionBarrier
            ));
            i++;
        }

        startWorkers();
    }

    private void startWorkers() {
        boidWorkers.forEach(BoidWorker::start);
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

    private void joinWorkers() {
        while (!boidWorkers.isEmpty()) {
            for (int i = 0; i < boidWorkers.size(); i++) {
                var w = boidWorkers.get(i);
                if (w.isAlive()) {
                    System.out.println("Interrupting: " + w);
                    w.interrupt();
                } else {
                    System.out.println("Removing: " + w);
                    boidWorkers.remove(w);
                }
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
                //System.out.println("Sleeping for " + (frameratePeriod - dtElapsed));
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
