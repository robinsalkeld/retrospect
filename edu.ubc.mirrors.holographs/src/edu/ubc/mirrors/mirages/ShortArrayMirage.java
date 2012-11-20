package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.LongArrayMirror;
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
    
    public static short getMirage(ShortArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getShort(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setMirage(ShortArrayMirror mirror, int index, short c) throws Throwable {
        try {
            mirror.setShort(index, c);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
}
