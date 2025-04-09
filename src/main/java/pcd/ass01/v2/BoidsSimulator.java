package pcd.ass01.v2;

import pcd.ass01.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class BoidsSimulator implements BoidsController {

    private final BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int N_WORKERS = Runtime.getRuntime().availableProcessors();
    private long t0;

    private List<Callable<Void>> calculateVelocityTaskList = new ArrayList<>();
    private List<Callable<Void>> updateVelocityTaskList = new ArrayList<>();
    private List<Callable<Void>> updatePositionTaskList = new ArrayList<>();

    private ExecutorService exc;
    private Flag runFlag, resetFlag;

    public BoidsSimulator(BoidsModel model, Flag runFlag, Flag resetFlag) {
        this.model = model;
        view = Optional.empty();
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
        exc = Executors.newFixedThreadPool(N_WORKERS);
    }

    private void initTasks() {
        var boids = model.getBoids();
        calculateVelocityTaskList.clear();
        updateVelocityTaskList.clear();
        updatePositionTaskList.clear();

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
        initTasks();
        if (view.isPresent()) {
            runSimulationWithView(view.get());
        } else {
            runFlag.set();
            runSimulationWithoutView();
        }
    }

    public void runSimulationWithView(BoidsView view) {
        while (true) {
            if (runFlag.isSet()) {
                t0 = System.currentTimeMillis();
                try {
                    waitForCompletion(exc.invokeAll(calculateVelocityTaskList));
                    waitForCompletion(exc.invokeAll(updateVelocityTaskList));
                    waitForCompletion(exc.invokeAll(updatePositionTaskList));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                view.update(framerate, new ArrayList<>(model.getBoids()));
                updateFrameRate(t0);
            }

            if (resetFlag.isSet()) {
                model.resetBoids(view.getNumberOfBoids());
                view.update(framerate, new ArrayList<>(model.getBoids()));
                notifyResetUnpressed();
                initTasks();
            }
        }
    }

    public void runSimulationWithoutView() {
        while (true) {
            System.out.println("[" + this + "] " + Thread.currentThread().getName() + " -> Running");
            try {
                waitForCompletion(exc.invokeAll(calculateVelocityTaskList));
                waitForCompletion(exc.invokeAll(updateVelocityTaskList));
                waitForCompletion(exc.invokeAll(updatePositionTaskList));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
        return framerate;
    }

    private void resetExecutor() {
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
    }
}