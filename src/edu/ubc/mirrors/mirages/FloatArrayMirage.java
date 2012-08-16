package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.FloatArrayMirror;

public class FloatArrayMirage extends ArrayMirage implements FloatArrayMirror {

    protected final FloatArrayMirror mirror;
    
    public FloatArrayMirage(FloatArrayMirror mirror) {
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
    
    public static float getMirage(FloatArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getFloat(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setMirage(FloatArrayMirror mirror, int index, float f) throws Throwable {
        try {
            mirror.setFloat(index, f);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
}
