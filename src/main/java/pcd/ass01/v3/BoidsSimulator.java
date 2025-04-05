package pcd.ass01.v3;

import pcd.ass01.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator implements BoidsController {

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private List<Thread> workers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES;
    private long t0;
    private Flag runFlag, resetFlag;

    private volatile MyCyclicBarrier computeVelocityBarrier;
    private volatile MyCyclicBarrier updateVelocityBarrier;
    private volatile MyCyclicBarrier updatePositionBarrier;
    private volatile MyCyclicBarrier updateGuiBarrier;

    public BoidsSimulator(BoidsModel model, Flag runFlag, Flag resetFlag) {
        this.model = model;
        view = Optional.empty();
        this.runFlag = runFlag;
        this.resetFlag = resetFlag;
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
        computeVelocityBarrier = new MyCyclicBarrier(boids.size());
        updateVelocityBarrier = new MyCyclicBarrier(boids.size());
        updatePositionBarrier = new MyCyclicBarrier(boids.size() + 1);
        updateGuiBarrier = new MyCyclicBarrier(boids.size() + 1);

        boids.forEach(boid -> {
            Thread t = Thread.ofVirtual().unstarted(new VirtualBoidWorker(boid,
                        model,
                        computeVelocityBarrier,
                        updateVelocityBarrier,
                        updatePositionBarrier,
                        updateGuiBarrier,
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
                updatePositionBarrier.await();
                view.update(framerate, new ArrayList<>(model.getBoids()));
                updateGuiBarrier.await();
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

    private void runSimulationWithoutView() {
        while (true) {
            System.out.println("[" + this + "] " + Thread.currentThread().getName() + " -> Running");
            updatePositionBarrier.await();
            updateGuiBarrier.await();
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