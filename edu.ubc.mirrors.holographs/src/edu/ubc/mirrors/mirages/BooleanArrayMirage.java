package edu.ubc.mirrors.mirages;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;

/**
 * Note that this class has to implement ByteArrayMirror as well since boolean arrays are actually
 * accessed with the BALOAD and BASTORE opcodes.
 * 
 * @author robinsalkeld
 */
public class BooleanArrayMirage extends ArrayMirage implements BooleanArrayMirror {

    protected final BooleanArrayMirror mirror;
    
    public BooleanArrayMirage(BooleanArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getBoolean(index);
    }

    @Override
    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        mirror.setBoolean(index, b);
    }

    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getBoolean(index) ? (byte)1 : (byte)0;
    }

    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        mirror.setBoolean(index, b != 0);
    }
    
    public static boolean getMirage(BooleanArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getBoolean(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setMirage(BooleanArrayMirror mirror, int index, boolean b) throws Throwable {
        try {
            mirror.setBoolean(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsMirage(mirror.getClassMirror().getVM(), e);
        }
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
