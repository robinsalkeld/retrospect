package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.ShortArrayMirror;

public class ShortArrayHologram extends ArrayHologram implements ShortArrayMirror {

    protected final ShortArrayMirror mirror;
    
    public ShortArrayHologram(ShortArrayMirror mirror) {
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
    
    public static short getHologram(ShortArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getShort(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(ShortArrayMirror mirror, int index, short c) throws Throwable {
        try {
            mirror.setShort(index, c);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
