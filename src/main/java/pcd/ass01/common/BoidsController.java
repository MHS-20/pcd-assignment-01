package pcd.ass01.common;

public interface BoidsController {
    public void notifyStart();
    public void notifyStop();
    public void notifyResetPressed();
    public void notifyResetUnpressed();
    public void runSimulation();
    public void attachView(BoidsView view);
    public int updateFrameRate(long t0);
}
