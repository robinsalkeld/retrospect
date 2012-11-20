package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.ByteArrayMirror;

public class ByteArrayMirage extends ArrayMirage implements ByteArrayMirror {

    protected final ByteArrayMirror mirror;
    
    public ByteArrayMirage(ByteArrayMirror mirror) {
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
    
    public static byte getMirage(ByteArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getByte(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setMirage(ByteArrayMirror mirror, int index, byte b) throws Throwable {
        try {
            mirror.setByte(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
}
