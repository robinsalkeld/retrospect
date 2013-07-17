package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.holograms.ArrayHologram;

public class ByteArrayHologram extends ArrayHologram implements ByteArrayMirror {

    protected final ByteArrayMirror mirror;
    
    public ByteArrayHologram(ByteArrayMirror mirror) {
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
    
    public static byte getHologram(ByteArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getByte(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(ByteArrayMirror mirror, int index, byte b) throws Throwable {
        try {
            mirror.setByte(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
