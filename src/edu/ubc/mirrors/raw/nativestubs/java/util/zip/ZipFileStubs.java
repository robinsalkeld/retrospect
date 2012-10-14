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
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeInstanceMirror;
import edu.ubc.mirrors.raw.NativeVirtualMachineMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class ZipFileStubs {

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
    
    public static int getTotal(Class<?> classLoaderLiteral, long jzfile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getZipFileForAddress(classLoaderLiteral, jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        
        return (Integer)getHostNativeMethod(ZipFile.class, "getTotal", Long.TYPE).invoke(null, hostJzfile);
    }
    
    public static int read(Class<?> classLoaderLiteral, long jzfile, long jzentry, long pos, Mirage b, int off, int len) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getZipFileForAddress(classLoaderLiteral, jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        checkEntry(jzentry);
        
        ByteArrayMirror bMirror = (ByteArrayMirror)b.getMirror();
        NativeByteArrayMirror nativeBMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, bMirror);
        byte[] nativeB = (byte[])nativeBMirror.getNativeObject();
        
        int result = (Integer)getHostNativeMethod(ZipFile.class, "read", Long.TYPE, Long.TYPE, Long.TYPE, byte[].class, Integer.TYPE, Integer.TYPE).invoke(null, hostJzfile, jzentry, pos, nativeB, off, len);
        
        SystemStubs.arraycopyMirrors(nativeBMirror, 0, bMirror, 0, bMirror.length());
        
        return result;
    }

    public static long open(Class<?> classLoaderLiteral, Mirage name, int mode, long lastModified, boolean usemmap) throws IOException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        String realName = Reflection.getRealStringForMirror((InstanceMirror)name.getMirror());
        File guestFile = new File(realName);
        File hostFile = vm.getMappedFile(guestFile, true);
        ZipFile zipFile = new JarFile(hostFile);
        long jzfile = getJzfile(zipFile);
        vm.zipFilesByAddress.put(jzfile, zipFile);
        return jzfile;
    }
    
    public static ZipFile getZipFileForAddress(Class<?> classLoaderLiteral, long jzfile) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        ZipFile hostZipFile = vm.zipFilesByAddress.get(jzfile);
        if (hostZipFile == null) {
            File path = vm.zipPathsByAddress.get(jzfile);
            if (path == null) {
                throw new InternalError();
            }
            File mappedPath = vm.getMappedFile(path, true);
            // Create a JarFile in case any of its native methods are invoked
            try {
                hostZipFile = new JarFile(mappedPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return hostZipFile;
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
    
    public static long getEntry(Class<?> classLoaderLiteral, long jzfile, Mirage name, boolean addSlash) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getZipFileForAddress(classLoaderLiteral, jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        
        ObjectMirror mirror = name.getMirror();
        Object nativeName;
        if (mirror instanceof ByteArrayMirror) {
            ByteArrayMirror nameMirror = (ByteArrayMirror)mirror;
            NativeByteArrayMirror nativeNameMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, nameMirror);
            nativeName = nativeNameMirror.getNativeObject();
        } else {
            nativeName = Reflection.getRealStringForMirror((InstanceMirror)mirror);
        }
        long hostJzentry = (Long)getHostNativeMethod(ZipFile.class, "getEntry", Long.TYPE, nativeName.getClass(), Boolean.TYPE).invoke(null, hostJzfile, nativeName, addSlash);
        return hostJzentry;
    }
    
    public static void freeEntry(Class<?> classLoaderLiteral, long jzfile, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getZipFileForAddress(classLoaderLiteral, jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        checkEntry(jzentry);
        
        getHostNativeMethod(ZipFile.class, "freeEntry", Long.TYPE, Long.TYPE).invoke(null, hostJzfile, jzentry);
    }

    
    private static void checkEntry(long jzentry) {
        // TODO-RS: Check that this entry was created within these stub methods, and not open from the original VM!
    }
    
    public static long getEntryTime(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getHostNativeMethod(ZipFile.class, "getEntryTime", Long.TYPE).invoke(null, jzentry);
    }
    
    public static long getEntryCrc(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getHostNativeMethod(ZipFile.class, "getEntryCrc", Long.TYPE).invoke(null, jzentry);
    }
    
    public static long getEntryCSize(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getHostNativeMethod(ZipFile.class, "getEntryCSize", Long.TYPE).invoke(null, jzentry);
    }
    
    public static long getEntrySize(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getHostNativeMethod(ZipFile.class, "getEntrySize", Long.TYPE).invoke(null, jzentry);
    }
    
    public static int getEntryMethod(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Integer)getHostNativeMethod(ZipFile.class, "getEntryMethod", Long.TYPE).invoke(null, jzentry);
    }
    
    public static int getEntryFlag(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Integer)getHostNativeMethod(ZipFile.class, "getEntryFlag", Long.TYPE).invoke(null, jzentry);
    }
    
    public static Mirage getCommentBytes(Class<?> classLoaderLiteral, long jzfile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getZipFileForAddress(classLoaderLiteral, jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        
        byte[] result = (byte[])getHostNativeMethod(ZipFile.class, "getCommentBytes", Long.TYPE).invoke(null, hostJzfile);
        
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        ArrayMirror resultMirror = Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(result));
        return ObjectMirage.make(resultMirror);
    }
    
    public static Mirage getEntryBytes(Class<?> classLoaderLiteral, long jzentry, int type) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        
        byte[] result = (byte[])getHostNativeMethod(ZipFile.class, "getEntryBytes", Long.TYPE, Integer.TYPE).invoke(null, jzentry, type);
        
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        ArrayMirror resultMirror = Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(result));
        return ObjectMirage.make(resultMirror);
    }
}
