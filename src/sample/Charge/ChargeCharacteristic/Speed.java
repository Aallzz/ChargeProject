package sample.Charge.ChargeCharacteristic;

/**
 * Created by Alexander on 05.12.2016.
 */

import java.lang.*;
import java.util.*;

public class Speed{

    final int mxSpeed = 100 * 10000;
    private Vector<Double> vector;

    public Speed(double vx, double vy, double vz){
        vector = new Vector<Double>(3);
        vector.addElement(vx);
        vector.addElement(vy);
        vector.addElement(vz);
    }

    public double get(int id){
        return (id < vector.size() ? vector.get(id) : -1);
    }

    public void set(int id, double value){
        if (id >= vector.size()) return ;
        vector.set(id, value);
    }

    public void addSpeedVector(Speed v){
        vector.set(0, vector.get(0) + v.get(0));
        vector.set(1, vector.get(1) + v.get(1));
        vector.set(2, vector.get(2) + v.get(2));

        for (int i = 0; i < 3; ++i){
            if (vector.get(i) >= mxSpeed){
                vector.set(i, mxSpeed - (vector.get(i) - mxSpeed));
            }
        }
    }

    public void addAccelerationVector(Acceleration a, double time){

    }
}
