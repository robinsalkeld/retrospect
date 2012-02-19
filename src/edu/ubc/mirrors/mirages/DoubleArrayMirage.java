package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.DoubleArrayMirror;

public class DoubleArrayMirage extends ArrayMirage implements DoubleArrayMirror {

    protected final DoubleArrayMirror mirror;
    
    public DoubleArrayMirage(DoubleArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getDouble(index);
    }

    @Override
    public void setDouble(int index, double b) throws ArrayIndexOutOfBoundsException {
        mirror.setDouble(index, b);
    }
}
