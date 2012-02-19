package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ShortArrayMirror;

public class ShortArrayMirage extends ArrayMirage implements ShortArrayMirror {

    protected final ShortArrayMirror mirror;
    
    public ShortArrayMirage(ShortArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public short getShort(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getShort(index);
    }

    @Override
    public void setShort(int index, short b) throws ArrayIndexOutOfBoundsException {
        mirror.setShort(index, b);
    }
}
