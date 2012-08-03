package edu.ubc.mirrors.raw.nativestubs.java.lang;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;

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
        String className = am.getClassMirror().getClassName();
        if (className.equals("[Z")) {
            return ((BooleanArrayMirror)am).getBoolean(index);
        } else if (className.equals("[B")) {
            return ((ByteArrayMirror)am).getByte(index);
        } else if (className.equals("[C")) {
            return ((CharArrayMirror)am).getChar(index);
        } else if (className.equals("[S")) {
            return ((ShortArrayMirror)am).getShort(index);
        } else if (className.equals("[I")) {
            return ((IntArrayMirror)am).getInt(index);
        } else if (className.equals("[J")) {
            return ((LongArrayMirror)am).getLong(index);
        } else if (className.equals("[F")) {
            return ((FloatArrayMirror)am).getFloat(index);
        } else if (className.equals("[D")) {
            return ((DoubleArrayMirror)am).getDouble(index);
        } else {
            return ((ObjectArrayMirror)am).get(index);
        }
    }
    
    private static void setArrayElement(ObjectMirror am, int index, Object o) {
        String className = am.getClassMirror().getClassName();
        if (className.equals("[Z")) {
            ((BooleanArrayMirror)am).setBoolean(index, (Boolean)o);
        } else if (className.equals("[B")) {
            ((ByteArrayMirror)am).setByte(index, (Byte)o);
        } else if (className.equals("[C")) {
            ((CharArrayMirror)am).setChar(index, (Character)o);
        } else if (className.equals("[S")) {
            ((ShortArrayMirror)am).setShort(index, (Short)o);
        } else if (className.equals("[I")) {
            ((IntArrayMirror)am).setInt(index, (Integer)o);
        } else if (className.equals("[J")) {
            ((LongArrayMirror)am).setLong(index, (Long)o);
        } else if (className.equals("[F")) {
            ((FloatArrayMirror)am).setFloat(index, (Float)o);
        } else if (className.equals("[D")) {
            ((DoubleArrayMirror)am).setDouble(index, (Double)o);
        } else {
            ((ObjectArrayMirror)am).set(index, (ObjectMirror)o);
        }
    }
    
    public static void setIn0(Class<?> classLoaderLiteral, Mirage stream) throws IllegalAccessException, NoSuchFieldException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        ClassMirror systemClass = vm.findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticField("in").set(stream.getMirror());
    }
    
    public static void setOut0(Class<?> classLoaderLiteral, Mirage stream) throws IllegalAccessException, NoSuchFieldException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        ClassMirror systemClass = vm.findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticField("out").set(stream.getMirror());
    }
    
    public static void setErr0(Class<?> classLoaderLiteral, Mirage stream) throws IllegalAccessException, NoSuchFieldException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        ClassMirror systemClass = vm.findBootstrapClassMirror(System.class.getName());
        systemClass.getStaticField("err").set(stream.getMirror());
    }
    
    // TODO-RS: I don't like this as a general rule, but it's called from 
    // ClassLoader#defineClass() in JDK 7 to measure loading time,
    // and also seems necessary in the read-only mapped fs.
    // It's probably actually okay, because this will just look like a very long
    // system delay to the original process, which is fairly reasonable.
    public static long nanoTime(Class<?> classLoaderLiteral) {
        return System.nanoTime();
    }
    
    public static long currentTimeMillis(Class<?> classLoaderLiteral) {
        return System.currentTimeMillis();
    }
    
}
