package sample.Charge.ChargeCharacteristic;

/**
 * Created by Alexander on 05.12.2016.
 */

import java.util.*;

public class Acceleration{
    private Vector<Double> vector;

    final int mxAcceleration = 100 * 10000 * 100;

    public Acceleration(double ax, double ay, double az){
        vector = new Vector<Double>(3);
        vector.addElement(ax);
        vector.addElement(ay);
        vector.addElement(az);
    }

    public double get(int id){
        return (id < vector.size() ? vector.get(id) : -1);
    }

    public void set(int id, double value){
        if (id >= vector.size()) return ;
        vector.set(id, value);
    }

    public void addAccelerationVector(Acceleration v){
        vector.set(0, vector.get(0) + v.get(0));
        vector.set(1, vector.get(1) + v.get(1));
        vector.set(2, vector.get(2) + v.get(2));
    }

}