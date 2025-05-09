package pcd.ass01.v3;

import pcd.ass01.common.BoidsModel;
import pcd.ass01.common.BoidsView;
import pcd.ass01.common.Flag;

import java.awt.*;

public class BoidsSimulation {

	final static int N_BOIDS = 1000;

	final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1000; 
	final static int ENVIRONMENT_HEIGHT = 1000;
    static final double MAX_SPEED = 4.0;
    static final double PERCEPTION_RADIUS = 50.0;
    static final double AVOID_RADIUS = 20.0;

	static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	final static int SCREEN_WIDTH = 1400;
	final static int SCREEN_HEIGHT = 600;

    public static void main(String[] args) {
    	var model = new BoidsModel(
    					N_BOIDS, 
    					SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT, 
    					ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
    					MAX_SPEED,
    					PERCEPTION_RADIUS,
    					AVOID_RADIUS);

		Flag runFlag = new Flag();
		Flag resetFlag = new Flag();

		var controller = new BoidsSimulator(model, runFlag, resetFlag);
		var view = new BoidsView(model, controller, SCREEN_WIDTH, SCREEN_HEIGHT, N_BOIDS);
		controller.attachView(view);
		controller.runSimulation();
    }
}