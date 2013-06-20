package edu.ubc.mirrors.raw.nativestubs.java.util.zip;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;

public class ZipFileStubs extends NativeStubs {

    public ZipFileStubs(ClassHolograph klass) {
	super(klass);
    }

//    private static final Map<org.objectweb.asm.commons.Method, Method> hostNativeMethods = 
//	         new HashMap<org.objectweb.asm.commons.Method, Method>();
    
    public static Method getHostNativeMethod(Class<?> klass, String name, Class<?> ... paramTypes) {
	// TODO-RS: Caching!!!
	try {
	    Method result = klass.getDeclaredMethod(name, paramTypes);
	    result.setAccessible(true);
	    return result;
	} catch (SecurityException e) {
	    throw new RuntimeException(e);
	} catch (NoSuchMethodException e) {
	    throw new RuntimeException(e);
	}
    }
    
    private static final Field jzfileField;
    static {
        try {
            jzfileField = ZipFile.class.getDeclaredField("jzfile");
            jzfileField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    public int getTotal(long jzfile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getVM().getZipFileForAddress(jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        
        return (Integer)getHostNativeMethod(ZipFile.class, "getTotal", Long.TYPE).invoke(null, hostJzfile);
    }
    
    public int read(long jzfile, long jzentry, long pos, ByteArrayMirror b, int off, int len) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getVM().getZipFileForAddress(jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        checkEntry(jzentry);
        
        NativeByteArrayMirror nativeBMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, b);
        byte[] nativeB = (byte[])nativeBMirror.getNativeObject();
        
        int result = (Integer)getHostNativeMethod(ZipFile.class, "read", Long.TYPE, Long.TYPE, Long.TYPE, byte[].class, Integer.TYPE, Integer.TYPE).invoke(null, hostJzfile, jzentry, pos, nativeB, off, len);
        
        Reflection.arraycopy(nativeBMirror, 0, b, 0, b.length());
        
        return result;
    }

    public long open(InstanceMirror name, int mode, long lastModified, boolean usemmap) throws IOException {
        VirtualMachineHolograph vm = getVM();
        
        String realName = Reflection.getRealStringForMirror(name);
        File guestFile = new File(realName);
        File hostFile = vm.getMappedFile(guestFile, true);
        ZipFile zipFile = new JarFile(hostFile);
        long jzfile = getJzfile(zipFile);
        vm.zipFilesByAddress.put(jzfile, zipFile);
        return jzfile;
    }
    
    private static long getJzfile(ZipFile zipFile) {
        try {
            return jzfileField.getLong(zipFile);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    // Java 1.6
    public long getEntry(long jzfile, InstanceMirror name, boolean addSlash) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getVM().getZipFileForAddress(jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        
        String realString = Reflection.getRealStringForMirror(name);
        NativeByteArrayMirror nativeNameMirror = new NativeByteArrayMirror(realString.getBytes());
        Object nativeName = nativeNameMirror.getNativeObject();
        long hostJzentry = (Long)getHostNativeMethod(ZipFile.class, "getEntry", Long.TYPE, nativeName.getClass(), Boolean.TYPE).invoke(null, hostJzfile, nativeName, addSlash);
        return hostJzentry;
    }
    
    // Java 1.7
    public long getEntry(long jzfile, ByteArrayMirror name, boolean addSlash) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getVM().getZipFileForAddress(jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        
        NativeByteArrayMirror nativeNameMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, name);
        Object nativeName = nativeNameMirror.getNativeObject();
        long hostJzentry = (Long)getHostNativeMethod(ZipFile.class, "getEntry", Long.TYPE, nativeName.getClass(), Boolean.TYPE).invoke(null, hostJzfile, nativeName, addSlash);
        return hostJzentry;
    }
    
    public void freeEntry(long jzfile, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getVM().getZipFileForAddress(jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        checkEntry(jzentry);
        
        getHostNativeMethod(ZipFile.class, "freeEntry", Long.TYPE, Long.TYPE).invoke(null, hostJzfile, jzentry);
    }

    
    private static void checkEntry(long jzentry) {
        // TODO-RS: Check that this entry was created within these stub methods, and not open from the original VM!
    }
    
    public long getTime(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return getEntryTime(jzentry);
    }
    public long getEntryTime(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getHostNativeMethod(ZipFile.class, "getEntryTime", Long.TYPE).invoke(null, jzentry);
    }
    
    public long getCrc(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return getEntryCrc(jzentry);
    }
    public long getEntryCrc(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getHostNativeMethod(ZipFile.class, "getEntryCrc", Long.TYPE).invoke(null, jzentry);
    }
    
    public long getCSize(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return getEntryCSize(jzentry);
    }
    public long getEntryCSize(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getHostNativeMethod(ZipFile.class, "getEntryCSize", Long.TYPE).invoke(null, jzentry);
    }
    
    public long getSize(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return getEntrySize(jzentry);
    }
    public long getEntrySize(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getHostNativeMethod(ZipFile.class, "getEntrySize", Long.TYPE).invoke(null, jzentry);
    }
    
    public int getMethod(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return getEntryMethod(jzentry);
    }
    public int getEntryMethod(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Integer)getHostNativeMethod(ZipFile.class, "getEntryMethod", Long.TYPE).invoke(null, jzentry);
    }
    
    public int getEntryFlag(long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Integer)getHostNativeMethod(ZipFile.class, "getEntryFlag", Long.TYPE).invoke(null, jzentry);
    }
    
    public ByteArrayMirror getCommentBytes(long jzfile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getVM().getZipFileForAddress(jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        
        byte[] result = (byte[])getHostNativeMethod(ZipFile.class, "getCommentBytes", Long.TYPE).invoke(null, hostJzfile);
        
        return (ByteArrayMirror)Reflection.copyArray(getVM(), (ArrayMirror)NativeInstanceMirror.makeMirror(result));
    }
    
    public ByteArrayMirror getEntryBytes(long jzentry, int type) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        
        byte[] result = (byte[])getHostNativeMethod(ZipFile.class, "getEntryBytes", Long.TYPE, Integer.TYPE).invoke(null, jzentry, type);
        
        return (ByteArrayMirror)Reflection.copyArray(getVM(), (ArrayMirror)NativeInstanceMirror.makeMirror(result));
    }
}
