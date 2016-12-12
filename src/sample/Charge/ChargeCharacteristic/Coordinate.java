/**
 * Created by Alexander on 05.12.2016.
 */

package sample.Charge.ChargeCharacteristic;

import java.lang.*;
import java.util.*;

public class Coordinate{
    private Vector<Double> vector;

    public Coordinate(double x, double y, double z){
        vector = new Vector<Double>(3);
        vector.addElement(x);
        vector.addElement(y);
        vector.addElement(z);
    }

    public double get(int id){
        return (id < vector.size() ? vector.get(id) : -1);
    }

    public void set(int id, double value){
        if (id >= vector.size()) return ;
        vector.set(id, value);
    }
}

