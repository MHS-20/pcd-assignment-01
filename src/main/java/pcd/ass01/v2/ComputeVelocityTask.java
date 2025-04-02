package pcd.ass01.v2;

import java.util.concurrent.Callable;

public class ComputeVelocityTask implements Callable<Void>{
    private final Boid boid;
    private final BoidsModel model;

    public ComputeVelocityTask(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }

    @Override
    public Void call() throws Exception {
        boid.calculateVelocity(model);
        return null;
    }

}
