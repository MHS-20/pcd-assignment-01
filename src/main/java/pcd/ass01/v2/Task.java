package pcd.ass01.v2;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

public class Task implements Callable<Void> {
    private final Boid boid;
    private final BoidsModel model;
    private final BiConsumer<Boid, BoidsModel> function;

    public Task(Boid boid, BoidsModel model, BiConsumer<Boid, BoidsModel> function) {
        this.boid = boid;
        this.model = model;
        this.function = function;
    }

    @Override
    public Void call() {
        function.accept(boid, model);
        return null;
    }
}