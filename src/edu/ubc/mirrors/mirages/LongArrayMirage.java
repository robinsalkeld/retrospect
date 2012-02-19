package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.LongArrayMirror;

public class LongArrayMirage extends ArrayMirage implements LongArrayMirror {

    protected final LongArrayMirror mirror;
    
    public LongArrayMirage(LongArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getLong(index);
    }

    @Override
    public void setLong(int index, long b) throws ArrayIndexOutOfBoundsException {
        mirror.setLong(index, b);
    }
}
