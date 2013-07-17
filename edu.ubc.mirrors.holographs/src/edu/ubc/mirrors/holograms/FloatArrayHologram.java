package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.holograms.ArrayHologram;

public class FloatArrayHologram extends ArrayHologram implements FloatArrayMirror {

    protected final FloatArrayMirror mirror;
    
    public FloatArrayHologram(FloatArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public float getFloat(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getFloat(index);
    }

    @Override
    public void setFloat(int index, float b) throws ArrayIndexOutOfBoundsException {
        mirror.setFloat(index, b);
    }
    
    public static float getHologram(FloatArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getFloat(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(FloatArrayMirror mirror, int index, float f) throws Throwable {
        try {
            mirror.setFloat(index, f);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
