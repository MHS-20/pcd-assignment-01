package pcd.ass01.v2;

import java.util.concurrent.Callable;

public class UpdateVelocityTask implements Callable<Void> {
    private final Boid boid;
    private final BoidsModel model;

    public UpdateVelocityTask(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }

    @Override
    public Void call() throws Exception {
        boid.updateVelocity(model);
        return null;
    }
}
