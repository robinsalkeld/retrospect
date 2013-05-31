package edu.ubc.mirrors.raw.nativestubs.java.util.zip;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;

public class InflaterStubs extends NativeStubs {

    public InflaterStubs(ClassHolograph klass) {
	super(klass);
    }

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
    
    public long init(boolean nowrap) throws IllegalArgumentException, IllegalAccessException {
        Inflater hostInflator = new Inflater(nowrap);
        long address = addressField.getLong(zsRefField.get(hostInflator));
        
        getVM().inflaterByAddress.put(address, hostInflator);
        return address;
    }
    
    public void reset(long addr) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Inflater hostInflater = getVM().getHostInflator(addr);
        long hostAddress = addressField.getLong(zsRefField.get(hostInflater));
        
        resetMethod.invoke(null, hostAddress);
    }

    public int inflateBytes(InstanceMirror inflatorMirror, long addr, ByteArrayMirror b, int off, int len) throws DataFormatException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        VirtualMachineHolograph vm = getVM();
        
        Inflater hostInflater = vm.getHostInflator(addr);
        long hostAddress = addressField.getLong(zsRefField.get(hostInflater));
        
        // Transfer the relevant fields onto the host inflater
        ByteArrayMirror bufMirror = (ByteArrayMirror)inflatorMirror.get(klass.getDeclaredField("buf"));
        NativeByteArrayMirror nativeBufMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, bufMirror);
        byte[] buf = (byte[])nativeBufMirror.getNativeObject();
        int bufOff = inflatorMirror.getInt(klass.getDeclaredField("off"));
        int bufLen = inflatorMirror.getInt(klass.getDeclaredField("len"));
        boolean finished = inflatorMirror.getBoolean(klass.getDeclaredField("finished"));
        boolean needsDict = inflatorMirror.getBoolean(klass.getDeclaredField("needDict"));
        hostInflater.setInput(buf, bufOff, bufLen);
        finishedField.setBoolean(hostInflater, finished);
        needDictField.setBoolean(hostInflater, needsDict);
        
        
        NativeByteArrayMirror nativeBMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, b);
        byte[] nativeB = (byte[])nativeBMirror.getNativeObject();
        
        int result = (Integer)inflateBytesMethod.invoke(hostInflater, hostAddress, nativeB, off, len);
        
        // Transfer the relevant fields back onto the guest inflater
        inflatorMirror.setInt(klass.getDeclaredField("off"), offField.getInt(hostInflater));
        inflatorMirror.setInt(klass.getDeclaredField("len"), lenField.getInt(hostInflater));
        inflatorMirror.setBoolean(klass.getDeclaredField("finished"), finishedField.getBoolean(hostInflater));
        inflatorMirror.setBoolean(klass.getDeclaredField("needDict"), needDictField.getBoolean(hostInflater));
        
        Reflection.arraycopy(nativeBMirror, 0, b, 0, b.length());
        
        return result;
    }
    
    public long getBytesWritten(long addr) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        VirtualMachineHolograph vm = getVM();
        
        Inflater hostInflater = vm.getHostInflator(addr);
        long hostAddress = addressField.getLong(zsRefField.get(hostInflater));
        
        return (Long) ZipFileStubs.getHostNativeMethod(Inflater.class, "getBytesWritten", long.class).invoke(null, hostAddress);
    }
}
