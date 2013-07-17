package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.holograms.ArrayHologram;

public class IntArrayHologram extends ArrayHologram implements IntArrayMirror {

    protected final IntArrayMirror mirror;
    
    public IntArrayHologram(IntArrayMirror mirror) {
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
    
    public static int getHologram(IntArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getInt(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(IntArrayMirror mirror, int index, int b) throws Throwable {
        try {
            mirror.setInt(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
