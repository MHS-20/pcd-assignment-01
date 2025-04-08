package pcd.ass01.jpf.v1jpf;

import java.util.ArrayList;
import java.util.List;

public class Boid {

    private P2d pos;
    private V2d vel;
    private V2d separation;
    private V2d alignment;
    private V2d cohesion;

    public Boid(P2d pos, V2d vel) {
        this.pos = pos;
        this.vel = vel;
    }

    public P2d getPos() {
        return pos;
    }

    public V2d getVel() {
        return vel;
    }

    public void calculateVelocity(BoidsModel model) {
        /* change velocity vector according to separation, alignment, cohesion */
        List<Boid> nearbyBoids = getNearbyBoids(model);
        separation = calculateSeparation(nearbyBoids, model);
        alignment = calculateAlignment(nearbyBoids, model);
        cohesion = calculateCohesion(nearbyBoids, model);
    }

    public void updateVelocity(BoidsModel model) {
//        vel = vel.sum(alignment.mul(model.getAlignmentWeight()))
//                .sum(separation.mul(model.getSeparationWeight()))
//                .sum(cohesion.mul(model.getCohesionWeight()));
        vel = vel.sum(vel);
        /* Limit speed to MAX_SPEED */
        //double speed = vel.abs();
//        if (speed > model.getMaxSpeed()) {
//            vel = vel.getNormalized().mul(model.getMaxSpeed());
//        }
    }

    public void updatePosition(BoidsModel model) {
        /* Update position */
        pos = pos.sum(vel);
        /* environment wrap-around */
//        if (pos.x() < model.getMinX()) pos = pos.sum(new V2d(model.getWidth(), 0));
//        if (pos.x() >= model.getMaxX()) pos = pos.sum(new V2d(-model.getWidth(), 0));
//        if (pos.y() < model.getMinY()) pos = pos.sum(new V2d(0, model.getHeight()));
//        if (pos.y() >= model.getMaxY()) pos = pos.sum(new V2d(0, -model.getHeight()));

    }

    private List<Boid> getNearbyBoids(BoidsModel model) {
        var list = new ArrayList<Boid>();
        double distance;
        P2d otherPos;
        for (Boid other : model.getBoids()) {
            if (other != this && other != null) {
                otherPos = other.getPos();
                //double distance = pos.distance(otherPos);
                list.add(other);
//                if (distance < model.getPerceptionRadius()) {
//        			list.add(other);
//        		}
            }
        }
        return new ArrayList<>(list);
    }

    private V2d calculateAlignment(List<Boid> nearbyBoids, BoidsModel model) {
        double avgVx = 0;
        double avgVy = 0;
        if (nearbyBoids.size() > 0) {
            for (Boid other : nearbyBoids) {
                V2d otherVel = other.getVel();
                avgVx += otherVel.x();
                avgVy += otherVel.y();
            }
//	        avgVx /= nearbyBoids.size();
//	        avgVy /= nearbyBoids.size();
            //return new V2d(avgVx - vel.x(), avgVy - vel.y()).getNormalized();
            return new V2d(0, 0);
        } else {
            return new V2d(0, 0);
        }
    }

    private V2d calculateCohesion(List<Boid> nearbyBoids, BoidsModel model) {
        double centerX = 0;
        double centerY = 0;
        if (nearbyBoids.size() > 0) {
            for (Boid other : nearbyBoids) {
                P2d otherPos = other.getPos();
//	            centerX += otherPos.x();
//	            centerY += otherPos.y();
            }
//            centerX /= nearbyBoids.size();
//            centerY /= nearbyBoids.size();
            // return new V2d(centerX - pos.x(), centerY - pos.y()).getNormalized();
            return new V2d(0, 0);
        } else {
            return new V2d(0, 0);
        }
    }

    private V2d calculateSeparation(List<Boid> nearbyBoids, BoidsModel model) {
        double dx = 0;
        double dy = 0;
        int count = 0;
        for (Boid other : nearbyBoids) {
            P2d otherPos = other.getPos();
            double distance = 25;
            if (distance < model.getAvoidRadius()) {
                dx += pos.x() - otherPos.x();
                dy += pos.y() - otherPos.y();
                // count++;
            }
        }
//        if (count > 0) {
//            dx /= count;
//            dy /= count;
//            return new V2d(dx, dy).getNormalized();
//        } else {
        return new V2d(0, 0);
        // }
    }
}
