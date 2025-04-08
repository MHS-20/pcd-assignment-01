package pcd.ass01.measurements.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class BoidsSimulator {

    private final BoidsModel model;
    //private Optional<BoidsView> view;

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES + 1;
    private long t0;

    private List<Callable<Void>> calculateVelocityTaskList = new ArrayList<>();
    private List<Callable<Void>> updateVelocityTaskList = new ArrayList<>();
    private List<Callable<Void>> updatePositionTaskList = new ArrayList<>();
    private ExecutorService exc;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        //view = Optional.empty();
        exc = Executors.newFixedThreadPool(N_WORKERS);
    }

    private void initTasks() {
        var boids = model.getBoids();

        boids.forEach(boid -> {
            calculateVelocityTaskList.add(new ComputeVelocityTask(boid, model));
            updateVelocityTaskList.add(new UpdateVelocityTask(boid, model));
            updatePositionTaskList.add(new UpdatePositionTask(boid, model));
        });
    }

    private void waitForCompletion(List<Future<Void>> futures) throws ExecutionException, InterruptedException {
        for (Future<Void> future : futures) {
            future.get();
        }
    }

//    public void attachView(BoidsView view) {
//        this.view = Optional.of(view);
//    }

    public void runSimulation() {
        long startTime = System.currentTimeMillis();
        initTasks();
        for (int i = 0; i < 1000; i++) {
            try {
                waitForCompletion(exc.invokeAll(calculateVelocityTaskList));
                waitForCompletion(exc.invokeAll(updateVelocityTaskList));
                waitForCompletion(exc.invokeAll(updatePositionTaskList));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
                ex.printStackTrace();
            }
            framerate = FRAMERATE;
        } else {
            framerate = (int) (1000 / dtElapsed);
        }
    }
}