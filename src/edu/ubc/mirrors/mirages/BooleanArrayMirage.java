package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.BooleanArrayMirror;

public class BooleanArrayMirage extends ArrayMirage implements BooleanArrayMirror {

    protected final BooleanArrayMirror mirror;
    
    public BooleanArrayMirage(BooleanArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getBoolean(index);
    }

    @Override
    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        mirror.setBoolean(index, b);
    }
}
