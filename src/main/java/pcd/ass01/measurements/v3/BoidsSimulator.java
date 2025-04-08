package pcd.ass01.measurements.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator {

    private final BoidsModel model;
    //private Optional<BoidsView> view;
    private List<Thread> workers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES + 1;
    private long t0;

    private MyCyclicBarrier computeVelocityBarrier;
    private MyCyclicBarrier updateVelocityBarrier;
    private MyCyclicBarrier updatePositionBarrier;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        //view = Optional.empty();
        initWorkers();
    }

    private void initWorkers() {
        workers.clear();

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

        var boids = model.getBoids();
        computeVelocityBarrier = new MyCyclicBarrier(boids.size(), "velocity1");
        updateVelocityBarrier = new MyCyclicBarrier(boids.size(), "velocity2");
        updatePositionBarrier = new MyCyclicBarrier(boids.size() + 1, "position");

        boids.forEach(boid -> {
            Thread t = Thread.ofVirtual().unstarted(() -> {
                while (true) {
                    boid.calculateVelocity(model);
                    computeVelocityBarrier.await();
                    boid.updateVelocity(model);
                    updateVelocityBarrier.await();
                    boid.updatePosition(model);
                    updatePositionBarrier.await();
                }
            });

            workers.add(t);
        });

        startWorkers();
    }

    private void startWorkers() {
        workers.forEach(Thread::start);
    }

//    public void attachView(BoidsView view) {
//        this.view = Optional.of(view);
//    }

    public void runSimulation() {
        long startTime = System.currentTimeMillis();
        initWorkers();

        for (int i = 0; i < 1000; i++) {
            updatePositionBarrier.await();
        }

        long endTime = System.currentTimeMillis();
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