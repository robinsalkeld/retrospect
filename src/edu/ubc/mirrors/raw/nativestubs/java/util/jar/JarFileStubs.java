package edu.ubc.mirrors.raw.nativestubs.java.util.jar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.nativestubs.java.util.zip.ZipFileStubs;

public class JarFileStubs {
    
    private static final Method getMetaInfEntryNamesMethod;
    static {
        try {
            getMetaInfEntryNamesMethod = JarFile.class.getDeclaredMethod("getMetaInfEntryNames");
            getMetaInfEntryNamesMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Mirage getMetaInfEntryNames(Class<?> classLoaderLiteral, Mirage jarFile) throws IllegalAccessException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        InstanceMirror jarFileMirror = (InstanceMirror)jarFile.getMirror();
        long jzfile = jarFileMirror.getMemberField("jzfile").getLong();
        JarFile hostJarFile = (JarFile)ZipFileStubs.getZipFileForAddress(classLoaderLiteral, jzfile);
        
        String[] result = (String[]) getMetaInfEntryNamesMethod.invoke(hostJarFile);
        
        ObjectArrayMirror resultMirror = (ObjectArrayMirror)vm.findBootstrapClassMirror(String.class.getName()).newArray(result.length);
        for (int i = 0; i < result.length; i++) {
            resultMirror.set(i, Reflection.makeString(vm, result[i]));
        }
        
        return ObjectMirage.make(resultMirror);
    }
    

}
