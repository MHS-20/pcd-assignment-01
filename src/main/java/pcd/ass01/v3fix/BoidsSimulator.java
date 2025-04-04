package pcd.ass01.v3fix;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator {

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private List<Thread> workers = new ArrayList<>();

    private static final int FRAMERATE = 50;
    private int framerate;
    private final int CORES = Runtime.getRuntime().availableProcessors();
    private final int N_WORKERS = CORES;
    private long t0;
    private volatile boolean loop = true;

    private volatile MyCyclicBarrier computeVelocityBarrier;
    private volatile MyCyclicBarrier updateVelocityBarrier;
    private volatile MyCyclicBarrier updatePositionBarrier;
    private volatile MyCyclicBarrier updateGuiBarrier;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
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
        updateGuiBarrier = new MyCyclicBarrier(boids.size() + 1, "gui");

        boids.forEach(boid -> {
            Thread t = Thread.ofVirtual().unstarted(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    // while (true) {
                    try {
                        boid.calculateVelocity(model);
                        computeVelocityBarrier.await();
                        boid.updateVelocity(model);
                        updateVelocityBarrier.await();

                        boid.updatePosition(model);
                        updatePositionBarrier.await();
                        updateGuiBarrier.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    //System.out.println(Thread.currentThread() + " " + Thread.currentThread().isInterrupted());
                }
                //System.out.println(Thread.currentThread() + " exiting");
            });

            workers.add(t);
        });

        startWorkers();
    }

    private void startWorkers() {
        System.out.println("Starting " + workers.size() + " workers");
        loop = true;
        workers.forEach(Thread::start);
    }

    private void stopWorkers() {
        System.out.println("Stopping " + workers.size() + " workers");
        loop = false;
        workers.forEach(Thread::interrupt);
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() throws InterruptedException {
        while (true) {
            if (view.isPresent()) {
                if (view.get().isRunning()) {
                    // System.out.println(Thread.currentThread() + " running");
                    t0 = System.currentTimeMillis();

                    System.out.println(Thread.currentThread() + " waiting on position");
                    updatePositionBarrier.await();

                    view.get().update(framerate);
                    updateFrameRate(t0);

                    System.out.println(Thread.currentThread() + " waiting on gui");
                    updateGuiBarrier.await();

                }

                if (view.get().isResetButtonPressed()) {
                    stopWorkers();
                    model.resetBoids(view.get().getNumberOfBoids());
                    view.get().update(framerate);
                    view.get().setResetButtonUnpressed();
                    initWorkers();
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