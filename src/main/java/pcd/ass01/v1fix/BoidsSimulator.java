package pcd.ass01.v1fix;

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

    private MyCyclicBarrierLocks computeVelocityBarrier;
    private MyCyclicBarrierLocks updateVelocityBarrier;
    private MyCyclicBarrierLocks updatePositionBarrier;
    private MyCyclicBarrierLocks updateGuiBarrier;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initWorkers();
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

        computeVelocityBarrier = new MyCyclicBarrierLocks(N_WORKERS);
        updateVelocityBarrier = new MyCyclicBarrierLocks(N_WORKERS);
        updatePositionBarrier = new MyCyclicBarrierLocks(N_WORKERS + 1);
        updateGuiBarrier = new MyCyclicBarrierLocks(N_WORKERS + 1);

        i = 0;
        for (List<Boid> partition : partitions) {
            boidWorkers.add(new BoidWorker("W" + i,
                    partition,
                    model,
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

    private void stopWorkers() {
        boidWorkers.forEach(BoidWorker::interrupt);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (true) {
            if (view.isPresent()) {
                if (view.get().isRunning()) {
                    t0 = System.currentTimeMillis();
                    updatePositionBarrier.await();
                    view.get().update(framerate);
                    updateGuiBarrier.await();
                    updateFrameRate(t0);
                }

                if (view.get().isResetButtonPressed()) {
                    stopWorkers();
                    model.resetBoids(view.get().getNumberOfBoids());
                    view.get().update(framerate);
                    initWorkers();
                    view.get().setResetButtonUnpressed();
                }
            }
        }
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
