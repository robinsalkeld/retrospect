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

    private static final Method openMethod;
    private static final Method getTotalMethod;
    private static final Method readMethod;
    private static final Method getEntryMethod;
    private static final Method getEntryTimeMethod;
    private static final Method getEntryCrcMethod;
    private static final Method getEntryCSizeMethod;
    private static final Method getEntrySizeMethod;
    private static final Method getEntryMethodMethod;
    private static final Method getEntryFlagMethod;
    private static final Method getCommentBytesMethod;
    private static final Method getEntryBytesMethod;
    private static final Method freeEntryMethod;
    private static final Field jzfileField;
    static {
        try {
            openMethod = ZipFile.class.getDeclaredMethod("open", String.class, Integer.TYPE, Long.TYPE, Boolean.TYPE);
            openMethod.setAccessible(true);
            
            getTotalMethod = ZipFile.class.getDeclaredMethod("getTotal", Long.TYPE);
            getTotalMethod.setAccessible(true);
            
            readMethod = ZipFile.class.getDeclaredMethod("read", Long.TYPE, Long.TYPE, Long.TYPE, byte[].class, Integer.TYPE, Integer.TYPE);
            readMethod.setAccessible(true);
            
            getEntryMethod = ZipFile.class.getDeclaredMethod("getEntry", Long.TYPE, byte[].class, Boolean.TYPE);
            getEntryMethod.setAccessible(true);
            
            getEntryTimeMethod = ZipFile.class.getDeclaredMethod("getEntryTime", Long.TYPE);
            getEntryTimeMethod.setAccessible(true);
            
            getEntryCrcMethod = ZipFile.class.getDeclaredMethod("getEntryCrc", Long.TYPE);
            getEntryCrcMethod.setAccessible(true);
            
            getEntryCSizeMethod = ZipFile.class.getDeclaredMethod("getEntryCSize", Long.TYPE);
            getEntryCSizeMethod.setAccessible(true);
            
            getEntrySizeMethod = ZipFile.class.getDeclaredMethod("getEntrySize", Long.TYPE);
            getEntrySizeMethod.setAccessible(true);
            
            getEntryMethodMethod = ZipFile.class.getDeclaredMethod("getEntryMethod", Long.TYPE);
            getEntryMethodMethod.setAccessible(true);
            
            getEntryFlagMethod = ZipFile.class.getDeclaredMethod("getEntryFlag", Long.TYPE);
            getEntryFlagMethod.setAccessible(true);
            
            getCommentBytesMethod = ZipFile.class.getDeclaredMethod("getCommentBytes", Long.TYPE);
            getCommentBytesMethod.setAccessible(true);
            
            getEntryBytesMethod = ZipFile.class.getDeclaredMethod("getEntryBytes", Long.TYPE, Integer.TYPE);
            getEntryBytesMethod.setAccessible(true);
            
            freeEntryMethod = ZipFile.class.getDeclaredMethod("freeEntry", Long.TYPE, Long.TYPE);
            freeEntryMethod.setAccessible(true);
            
            jzfileField = ZipFile.class.getDeclaredField("jzfile");
            jzfileField.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static int getTotal(Class<?> classLoaderLiteral, long jzfile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getZipFileForAddress(classLoaderLiteral, jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        
        return (Integer)getTotalMethod.invoke(null, hostJzfile);
    }
    
    public static int read(Class<?> classLoaderLiteral, long jzfile, long jzentry, long pos, Mirage b, int off, int len) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getZipFileForAddress(classLoaderLiteral, jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        checkEntry(jzentry);
        
        ByteArrayMirror bMirror = (ByteArrayMirror)b.getMirror();
        NativeByteArrayMirror nativeBMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, bMirror);
        byte[] nativeB = (byte[])nativeBMirror.getNativeObject();
        
        int result = (Integer)readMethod.invoke(null, hostJzfile, jzentry, pos, nativeB, off, len);
        
        SystemStubs.arraycopyMirrors(nativeBMirror, 0, bMirror, 0, bMirror.length());
        
        return result;
    }

    public static long open(Class<?> classLoaderLiteral, Mirage name, int mode, long lastModified, boolean usemmap) throws IOException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        String realName = Reflection.getRealStringForMirror((InstanceMirror)name.getMirror());
        File guestFile = new File(realName);
        File hostFile = vm.getMappedFile(guestFile);
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
            File mappedPath = vm.getMappedFile(path);
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
        
        ByteArrayMirror nameMirror = (ByteArrayMirror)name.getMirror();
        NativeByteArrayMirror nativeNameMirror = (NativeByteArrayMirror)Reflection.copyArray(NativeVirtualMachineMirror.INSTANCE, nameMirror);
        byte[] nativeName = (byte[])nativeNameMirror.getNativeObject();
                
        long hostJzentry = (Long)getEntryMethod.invoke(null, hostJzfile, nativeName, addSlash);
        return hostJzentry;
    }
    
    public static void freeEntry(Class<?> classLoaderLiteral, long jzfile, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getZipFileForAddress(classLoaderLiteral, jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        checkEntry(jzentry);
        
        freeEntryMethod.invoke(null, hostJzfile, jzentry);
    }

    
    private static void checkEntry(long jzentry) {
        // TODO-RS: Check that this entry was created within these stub methods, and not open from the original VM!
    }
    
    public static long getEntryTime(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getEntryTimeMethod.invoke(null, jzentry);
    }
    
    public static long getEntryCrc(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getEntryCrcMethod.invoke(null, jzentry);
    }
    
    public static long getEntryCSize(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getEntryCSizeMethod.invoke(null, jzentry);
    }
    
    public static long getEntrySize(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Long)getEntrySizeMethod.invoke(null, jzentry);
    }
    
    public static int getEntryMethod(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Integer)getEntryMethodMethod.invoke(null, jzentry);
    }
    
    public static int getEntryFlag(Class<?> classLoaderLiteral, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        return (Integer)getEntryFlagMethod.invoke(null, jzentry);
    }
    
    public static Mirage getCommentBytes(Class<?> classLoaderLiteral, long jzfile) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ZipFile hostZipFile = getZipFileForAddress(classLoaderLiteral, jzfile);
        long hostJzfile = getJzfile(hostZipFile);
        
        byte[] result = (byte[])getCommentBytesMethod.invoke(null, hostJzfile);
        
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        ArrayMirror resultMirror = Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(result));
        return ObjectMirage.make(resultMirror);
    }
    
    public static Mirage getEntryBytes(Class<?> classLoaderLiteral, long jzentry, int type) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        checkEntry(jzentry);
        
        byte[] result = (byte[])getEntryBytesMethod.invoke(null, jzentry, type);
        
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        ArrayMirror resultMirror = Reflection.copyArray(vm, (ArrayMirror)NativeInstanceMirror.makeMirror(result));
        return ObjectMirage.make(resultMirror);
    }
}
