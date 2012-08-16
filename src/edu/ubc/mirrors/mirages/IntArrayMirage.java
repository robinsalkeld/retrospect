package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.IntArrayMirror;

public class IntArrayMirage extends ArrayMirage implements IntArrayMirror {

    protected final IntArrayMirror mirror;
    
    public IntArrayMirage(IntArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getInt(index);
    }

    @Override
    public void setInt(int index, int b) throws ArrayIndexOutOfBoundsException {
        mirror.setInt(index, b);
    }
    
    public static int getMirage(IntArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getInt(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setMirage(IntArrayMirror mirror, int index, int b) throws Throwable {
        try {
            mirror.setInt(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
}
