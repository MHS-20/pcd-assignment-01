package pcd.ass01.measurements.v1;

import java.util.ArrayList;
import java.util.List;

public class BoidsSimulator {

    private final BoidsModel model;
   // private Optional<BoidsView> view;
    private final List<BoidWorker> boidWorkers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES;
    private long t0;
    private long startTime, endTime;

    private MyCyclicBarrier computeVelocityBarrier;
    private MyCyclicBarrier updateVelocityBarrier;
    private MyCyclicBarrier updatePositionBarrier;
    private MyCyclicBarrier updateGuiBarrier;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        //view = Optional.empty();
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

        computeVelocityBarrier = new MyCyclicBarrier(N_WORKERS);
        updateVelocityBarrier = new MyCyclicBarrier(N_WORKERS);
        updatePositionBarrier = new MyCyclicBarrier(N_WORKERS + 1);
        updateGuiBarrier = new MyCyclicBarrier(N_WORKERS + 1);

        i = 0;
        for (List<Boid> partition : partitions) {
            boidWorkers.add(new BoidWorker("W" + i,
                    partition,
                    model,
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

//    public void attachView(BoidsView view) {
//        this.view = Optional.of(view);
//    }

    public void runSimulation() {
        startTime = System.currentTimeMillis();
        initWorkers();

        for (int i = 0; i < 1000; i++) {
            updatePositionBarrier.await();
        }

        endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
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
