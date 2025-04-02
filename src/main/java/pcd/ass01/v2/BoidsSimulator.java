package pcd.ass01.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class BoidsSimulator {

    private final BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES + 1;
    private long t0;

    private List<Callable<Void>> calculateVelocityTaskList = new ArrayList<>();
    private List<Callable<Void>> updateVelocityTaskList = new ArrayList<>();
    private List<Callable<Void>> updatePositionTaskList = new ArrayList<>();
    private volatile boolean loop = true;
    private ExecutorService exc;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        initTasks();
    }

    private void initTasks() {
        var boids = model.getBoids();

        if (exc != null && !exc.isShutdown()) {
            exc.shutdown();
            try {
                if (!exc.awaitTermination(1, TimeUnit.SECONDS)) {
                    exc.shutdownNow();
                }
            } catch (InterruptedException e) {
                exc.shutdownNow();
            }
        }

        exc = Executors.newFixedThreadPool(N_WORKERS);

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

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (true) {
            if (view.isPresent()) {
                System.out.println(view.get().isRunning());
                if (view.get().isRunning()) {
                    //System.out.println("running simulation");
                    t0 = System.currentTimeMillis();

                    try {
                        waitForCompletion(exc.invokeAll(calculateVelocityTaskList));
                        waitForCompletion(exc.invokeAll(updateVelocityTaskList));
                        waitForCompletion(exc.invokeAll(updatePositionTaskList));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    view.get().update(framerate);
                    updateFrameRate(t0);
                }

                if (view.get().isResetButtonPressed()) {
                    model.resetBoids(view.get().getNumberOfBoids());
                    view.get().update(framerate);
                    initTasks();
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
                ex.printStackTrace();
            }
            framerate = FRAMERATE;
        } else {
            framerate = (int) (1000 / dtElapsed);
        }
    }
}