package pcd.ass01.v1;

import pcd.ass01.common.Boid;
import pcd.ass01.common.BoidsModel;
import pcd.ass01.common.BoidsView;
import pcd.ass01.common.MyCyclicBarrier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator {

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private final List<BoidWorker> boidWorkers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES;
    private long t0;

    private MyCyclicBarrier computeVelocityBarrier;
    private MyCyclicBarrier updateVelocityBarrier;
    private MyCyclicBarrier updatePositionBarrier;
    private MyCyclicBarrier updateGuiBarrier;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        //initWorkers();
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

        computeVelocityBarrier = new MyCyclicBarrier(N_WORKERS);
        updateVelocityBarrier = new MyCyclicBarrier(N_WORKERS);
        updatePositionBarrier = new MyCyclicBarrier(N_WORKERS + 1);
        updateGuiBarrier = new MyCyclicBarrier(N_WORKERS + 1);

        i = 0;
        for (List<Boid> partition : partitions) {
            boidWorkers.add(new BoidWorker("W" + i,
                    partition,
                    model,
                    view,
                    computeVelocityBarrier,
                    updateVelocityBarrier,
                    updatePositionBarrier,
                    updateGuiBarrier
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
            runSimulationWithoutView();
        }
    }

    private void runSimulationWithView(BoidsView view) {
        while (true) {
            if (view.isRunning()) {
                t0 = System.currentTimeMillis();
                updatePositionBarrier.await();
                view.update(framerate, new ArrayList<>(model.getBoids()));
                updateGuiBarrier.await();
                updateFrameRate(t0);
            }

            if (view.isResetButtonPressed()) {
                model.resetBoids(view.getNumberOfBoids());
                view.update(framerate, new ArrayList<>(model.getBoids()));
                view.setResetButtonUnpressed();
                initWorkers();
            }
        }
    }

    private void runSimulationWithoutView() {
        while (true) {
            updatePositionBarrier.await();
            updateGuiBarrier.await();
        }
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
