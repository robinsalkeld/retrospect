package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ByteArrayMirror;

public class ByteArrayMirage extends ArrayMirage implements ByteArrayMirror {

    protected final ByteArrayMirror mirror;
    
    public ByteArrayMirage(ByteArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getByte(index);
    }

    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        mirror.setByte(index, b);
    }
}
