package pcd.ass01.measurements.v0;

public class BoidsSimulator {

    private BoidsModel model;
    //private Optional<BoidsView> view;
    
    private static final int FRAMERATE = 25;
    private int framerate;
    
    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        //view = Optional.empty();
    }

//    public void attachView(BoidsView view) {
//    	this.view = Optional.of(view);
//    }
      
    public void runSimulation() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
    		var boids = model.getBoids();
    		for (Boid boid : boids) {
                boid.updateVelocity(model);
            }
    		for (Boid boid : boids) {
                boid.updatePos(model);
            }
    	}
        long endTime = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (endTime - startTime) + " ms");
    }
}
