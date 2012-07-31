package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;

/**
 * Note that this class has to implement ByteArrayMirror as well since boolean arrays are actually
 * accessed with the BALOAD and BASTORE opcodes.
 * 
 * @author robinsalkeld
 */
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

    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getBoolean(index) ? (byte)1 : (byte)0;
    }

    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        mirror.setBoolean(index, b != 0);
    }
}
