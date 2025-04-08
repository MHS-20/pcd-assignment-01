package pcd.ass01.jpf.v1jpf;

import java.util.ArrayList;
import java.util.List;

public class BoidsSimulator {

    private final BoidsModel model;
    private final List<BoidWorker> boidWorkers = new ArrayList<>();
    private final int N_WORKERS = 2;

    private MyCyclicBarrier computeVelocityBarrier;
    private MyCyclicBarrier updateVelocityBarrier;
    private MyCyclicBarrier updatePositionBarrier;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
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

    public void runSimulation() {
        // while (true) {
        for (int i = 0; i < 2; i++) {
            updatePositionBarrier.await();
            model.resetBoids(2);
        }
        // }
    }
}
