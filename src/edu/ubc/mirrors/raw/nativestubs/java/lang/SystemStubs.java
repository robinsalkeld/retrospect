package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.mirages.Mirage;

public class SystemStubs {

    public static int identityHashCode(Class<?> classLoaderLiteral, Mirage o) {
        return System.identityHashCode(o);
    }
    
    public static void arraycopy(Class<?> classLoaderLiteral, Mirage src, int srcPos, Mirage dest, int destPos, int length) {
        arraycopyMirrors(src.getMirror(), srcPos, dest.getMirror(), destPos, length);
    }
    
    public static void arraycopyMirrors(ObjectMirror src, int srcPos, ObjectMirror dest, int destPos, int length) {
        for (int off = 0; off < length; off++) {
            setArrayElement(dest, destPos + off, getArrayElement(src, srcPos + off));
        }
    }
    
    private static Object getArrayElement(ObjectMirror am, int index) {
        if (am instanceof ObjectArrayMirror) {
            return ((ObjectArrayMirror)am).get(index);
        } else if (am instanceof BooleanArrayMirror) {
            return ((BooleanArrayMirror)am).getBoolean(index);
        } else if (am instanceof ByteArrayMirror) {
            return ((ByteArrayMirror)am).getByte(index);
        } else if (am instanceof CharArrayMirror) {
            return ((CharArrayMirror)am).getChar(index);
        } else if (am instanceof ShortArrayMirror) {
            return ((ShortArrayMirror)am).getShort(index);
        } else if (am instanceof IntArrayMirror) {
            return ((IntArrayMirror)am).getInt(index);
        } else if (am instanceof LongArrayMirror) {
            return ((LongArrayMirror)am).getLong(index);
        } else if (am instanceof FloatArrayMirror) {
            return ((FloatArrayMirror)am).getFloat(index);
        } else if (am instanceof DoubleArrayMirror) {
            return ((DoubleArrayMirror)am).getDouble(index);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    private static void setArrayElement(ObjectMirror am, int index, Object o) {
        if (am instanceof ObjectArrayMirror) {
            ((ObjectArrayMirror)am).set(index, (ObjectMirror)o);
        } else if (am instanceof BooleanArrayMirror) {
            ((BooleanArrayMirror)am).setBoolean(index, (Boolean)o);
        } else if (am instanceof ByteArrayMirror) {
            ((ByteArrayMirror)am).setByte(index, (Byte)o);
        } else if (am instanceof CharArrayMirror) {
            ((CharArrayMirror)am).setChar(index, (Character)o);
        } else if (am instanceof ShortArrayMirror) {
            ((ShortArrayMirror)am).setShort(index, (Short)o);
        } else if (am instanceof IntArrayMirror) {
            ((IntArrayMirror)am).setInt(index, (Integer)o);
        } else if (am instanceof LongArrayMirror) {
            ((LongArrayMirror)am).setLong(index, (Long)o);
        } else if (am instanceof FloatArrayMirror) {
            ((FloatArrayMirror)am).setFloat(index, (Float)o);
        } else if (am instanceof DoubleArrayMirror) {
            ((DoubleArrayMirror)am).setDouble(index, (Double)o);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
