package pcd.ass01.v1;

import pcd.ass01.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator implements BoidsController {

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private final List<BoidWorker> boidWorkers = new ArrayList<>();

    private static final int MAX_FRAMERATE = 50;
    private int framerate;
    private final int N_WORKERS = Runtime.getRuntime().availableProcessors();

    private MasterAgent master;
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
        // awakes suspended threads
        computeVelocityBarrier.breaks();
        updateVelocityBarrier.breaks();
        updatePositionBarrier.breaks();

        try {
            //checks all threads have terminated
            for (BoidWorker w : boidWorkers)
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
                //System.out.println("Sleeping for " + (frameratePeriod - dtElapsed));
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
