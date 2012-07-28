package edu.ubc.mirrors.raw.nativestubs.java.util.zip;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipFile;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class InflaterStubs {

    private static final Method resetMethod;
    private static final Method inflateBytesMethod;
    private static final Field offField;
    private static final Field lenField;
    private static final Field finishedField;
    private static final Field needDictField;
    
    
    private static final Field zsRefField;
    private static final Field addressField;
    static {
        try {
            resetMethod = Inflater.class.getDeclaredMethod("reset", Long.TYPE);
            resetMethod.setAccessible(true);
            
            inflateBytesMethod = Inflater.class.getDeclaredMethod("inflateBytes", Long.TYPE, byte[].class, Integer.TYPE, Integer.TYPE);
            inflateBytesMethod.setAccessible(true);
            
            offField = Inflater.class.getDeclaredField("off");
            offField.setAccessible(true);
            
            lenField = Inflater.class.getDeclaredField("len");
            lenField.setAccessible(true);
            
            finishedField = Inflater.class.getDeclaredField("finished");
            finishedField.setAccessible(true);
            
            needDictField = Inflater.class.getDeclaredField("needDict");
            needDictField.setAccessible(true);
            
            zsRefField = Inflater.class.getDeclaredField("zsRef");
            zsRefField.setAccessible(true);
            
            addressField = zsRefField.getType().getDeclaredField("address");
            addressField.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static long init(Class<?> classLoaderLiteral, boolean nowrap) throws IllegalArgumentException, IllegalAccessException {
        Inflater hostInflator = new Inflater(nowrap);
        long address = addressField.getLong(zsRefField.get(hostInflator));
        
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        vm.inflaterByAddress.put(address, hostInflator);
        return address;
    }
    
    public static void reset(Class<?> classLoaderLiteral, long addr) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        resetMethod.invoke(null, addr);
    }

    public static int inflateBytes(Class<?> classLoaderLiteral, Mirage inflater, long addr, Mirage b, int off, int len) throws DataFormatException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        Inflater hostInflater = vm.inflaterByAddress.get(addr);
        
        // Transfer the relevant fields onto the host inflater
        InstanceMirror inflatorMirror = (InstanceMirror)inflater.getMirror(); 
        ByteArrayMirror bufMirror = (ByteArrayMirror)inflatorMirror.getMemberField("buf").get();
        NativeByteArrayMirror nativeBufMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, bufMirror);
        byte[] buf = (byte[])nativeBufMirror.getNativeObject();
        int bufOff = inflatorMirror.getMemberField("off").getInt();
        int bufLen = inflatorMirror.getMemberField("len").getInt();
        boolean finished = inflatorMirror.getMemberField("finished").getBoolean();
        boolean needsDict = inflatorMirror.getMemberField("needDict").getBoolean();
        hostInflater.setInput(buf, bufOff, bufLen);
        finishedField.setBoolean(hostInflater, finished);
        needDictField.setBoolean(hostInflater, needsDict);
        
        
        ByteArrayMirror bMirror = (ByteArrayMirror)b.getMirror();
        NativeByteArrayMirror nativeBMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, bMirror);
        byte[] nativeB = (byte[])nativeBMirror.getNativeObject();
        
        int result = (Integer)inflateBytesMethod.invoke(hostInflater, addr, nativeB, off, len);
        
        // Transfer the relevant fields back onto the guest inflater
        inflatorMirror.getMemberField("off").setInt(offField.getInt(hostInflater));
        inflatorMirror.getMemberField("len").setInt(lenField.getInt(hostInflater));
        inflatorMirror.getMemberField("finished").setBoolean(finishedField.getBoolean(hostInflater));
        inflatorMirror.getMemberField("needDict").setBoolean(needDictField.getBoolean(hostInflater));
        
        SystemStubs.arraycopyMirrors(nativeBMirror, 0, bMirror, 0, bMirror.length());
        
        return result;
    }
    
}
