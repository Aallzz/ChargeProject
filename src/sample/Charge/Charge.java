package sample.Charge;

/**
 * Created by Alexander on 05.12.2016.
 */

import java.util.*;
import sample.Charge.ChargeCharacteristic.*;

public class Charge{

    public static double m = 1;
    public static double r = 2;
    public static double k = 90000000000000.0;
    public static double alpha = 0.0000000000000000001/2;
    public static double betta  = 0.0000000000000000001;

    private Speed speed;
    private Acceleration acceleration;
    private Coordinate coordinate;
    private double q = 1;
    public Force force;
    private boolean fixed = false;

    public Charge(double qq, Coordinate c, Speed sp, Acceleration a, boolean fx){
        fixed = fx;
        q = qq;
        coordinate = c;
        speed = sp;
        acceleration = a;
        force = new Force(0.0, 0.0, 0.0);
    }

    public double getQ(){ return q; }
    public Coordinate getC(){ return coordinate; }
    public Acceleration getA(){ return acceleration; }
    public Force getF(){ return force; }
    public boolean isFixed() {return fixed; }

    public Speed getS(){ return speed; }
    public void setQ(double newQ){ q = newQ; }


    public void updateCoordinate(double time){
        for (int i = 0; i < 3; ++i)
            coordinate.set(i, coordinate.get(i) + speed.get(i) * time + acceleration.get(i) * time * time / 2.0);
    }

    public void updateSpeed(double time){

        for (int i = 0; i < 3; ++i)
            speed.set(i, speed.get(i) + acceleration.get(i) * time);
    }

    public void updateAcceleration(){
        for (int i = 0; i < 3; ++i)
            acceleration.set(i, force.get(i) / m);
    }


    public static double distance(Charge c1, Charge c2){
        double d = 0.0;
        for (int i = 0; i < 3; ++i){
            d += Math.pow((c1.getC().get(i) - c2.getC().get(i)),2);
        }
        d = Math.pow(d, 0.5);
        return d;
    }

    public static double distance(Coordinate c1, Coordinate c2){
        double d = 0.0;
        for (int i = 0; i < 3; ++i){
            // System.out.println(c1.get(i) - c2.get(i));
            d += Math.pow((c1.get(i) - c2.get(i)),2);
        }
        d = Math.pow(d, 0.5);
        return d;
    }

    public void updateForce(Charge ch){
        double r = distance(coordinate, ch.getC());
        r /= 10000.0;
        // System.out.println("R = " + r);


        // System.out.println("Q = " + q + " " + ch.getQ());
        double force_value = -k * q * ch.getQ() / Math.pow(r, 2);
        force_value -= alpha / Math.pow(r, 12) - betta / Math.pow(r, 6);
        force_value /= 50;

        r *= 10000.0;
        Force curForce = new Force(force_value * (-coordinate.get(0) + ch.getC().get(0)) / r,
                force_value * (-coordinate.get(1) + ch.getC().get(1)) / r, 0.0);
        //  force_value * r / (-coordinate.get(2) + ch.getC().get(2)));

        // System.out.println("FORCE");
        // for (int i = 0; i < 3; ++i) System.out.println(curForce.get(i));
        force.addForceVector(curForce);
    }
}
