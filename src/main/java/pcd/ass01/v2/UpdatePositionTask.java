package pcd.ass01.v2;

import java.util.concurrent.Callable;

public class UpdatePositionTask implements Callable<Void> {
    private final Boid boid;
    private final BoidsModel model;

    public UpdatePositionTask(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }

    public Void call() throws Exception {
        boid.updatePosition(model);
        return null;
    }
}
