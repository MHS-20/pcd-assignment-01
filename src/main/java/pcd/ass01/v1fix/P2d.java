package pcd.ass01.v1fix;

/**
 *
 * 2-dimensional point
 * objects are completely state-less
 *
 */
public final class P2d {

    public double x;
    public double y;

    public P2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public P2d sum(V2d v){
        return new P2d(x+v.x(),y+v.y());
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public V2d sub(P2d v){
        return new V2d(x-v.x,y-v.y);
    }
    
    public double distance(P2d p) {
    	double dx = p.x - x;
    	double dy = p.y - y;
    	return Math.sqrt(dx*dx + dy*dy);

    }
    
    public String toString(){
        return "P2d("+x+","+y+")";
    }

}
