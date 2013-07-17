package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.holograms.ArrayHologram;

public class LongArrayHologram extends ArrayHologram implements LongArrayMirror {

    protected final LongArrayMirror mirror;
    
    public LongArrayHologram(LongArrayMirror mirror) {
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
    
    public static long getHologram(LongArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getLong(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(LongArrayMirror mirror, int index, long l) throws Throwable {
        try {
            mirror.setLong(index, l);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
