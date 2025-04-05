package pcd.ass01.v3;

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

                if (view.isPresent()) {
                    while (!view.get().isResetButtonPressed()) {
                        while (view.get().isRunning()) {
                            boid.calculateVelocity(model);
                            computeVelocityBarrier.await();

                            boid.updateVelocity(model);
                            updateVelocityBarrier.await();

                            boid.updatePosition(model);
                            updatePositionBarrier.await();
                            updateGuiBarrier.await();
                        }
                    }
                }else{ // run without view
                    while(true){
                        boid.calculateVelocity(model);
                        computeVelocityBarrier.await();

                        boid.updateVelocity(model);
                        updateVelocityBarrier.await();

                        boid.updatePosition(model);
                        updatePositionBarrier.await();
                        updateGuiBarrier.await();
                    }
                }
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