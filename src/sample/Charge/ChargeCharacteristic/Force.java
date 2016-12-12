package sample.Charge.ChargeCharacteristic;

/**
 * Created by Alexander on 05.12.2016.
 */

import java.util.*;

public class Force{
    private Vector<Double> vector;

    public Force(double fx, double fy, double fz){
        vector = new Vector<Double>(3);
        vector.addElement(fx);
        vector.addElement(fy);
        vector.addElement(fz);
    }

    public double get(int id){
        return (id < vector.size() ? vector.get(id) : -1);
    }

    public void set(int id, double value){
        if (id >= vector.size()) return ;
        vector.set(id, value);
    }

    public void addForceVector(Force v){
        vector.set(0, vector.get(0) + v.get(0));
        vector.set(1, vector.get(1) + v.get(1));
        vector.set(2, vector.get(2) + v.get(2));
    }

    public void clear(){
        for (int i = 0; i < 3; ++i) vector.set(i, 0.0);
    }
}