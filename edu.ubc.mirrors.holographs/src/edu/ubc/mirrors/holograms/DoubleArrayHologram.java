package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.holograms.ArrayHologram;

public class DoubleArrayHologram extends ArrayHologram implements DoubleArrayMirror {

    protected final DoubleArrayMirror mirror;
    
    public DoubleArrayHologram(DoubleArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public double getDouble(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getDouble(index);
    }

    @Override
    public void setDouble(int index, double b) throws ArrayIndexOutOfBoundsException {
        mirror.setDouble(index, b);
    }
    
    public static double getHologram(DoubleArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getDouble(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(DoubleArrayMirror mirror, int index, double d) throws Throwable {
        try {
            mirror.setDouble(index, d);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
