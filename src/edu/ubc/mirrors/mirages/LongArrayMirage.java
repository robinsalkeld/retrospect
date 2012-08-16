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
    
    public static long getMirage(LongArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getLong(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setMirage(LongArrayMirror mirror, int index, long l) throws Throwable {
        try {
            mirror.setLong(index, l);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
}
