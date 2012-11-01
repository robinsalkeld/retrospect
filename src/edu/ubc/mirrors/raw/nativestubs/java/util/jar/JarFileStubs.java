package edu.ubc.mirrors.raw.nativestubs.java.util.jar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarFile;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class JarFileStubs extends NativeStubs {
    
    public JarFileStubs(ClassHolograph klass) {
	super(klass);
    }

    private static final Method getMetaInfEntryNamesMethod;
    static {
        try {
            getMetaInfEntryNamesMethod = JarFile.class.getDeclaredMethod("getMetaInfEntryNames");
            getMetaInfEntryNamesMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Mirage getMetaInfEntryNames(Mirage jarFile) throws IllegalAccessException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException {
        VirtualMachineHolograph vm = getVM();
        
        InstanceMirror jarFileMirror = (InstanceMirror)jarFile.getMirror();
        long jzfile = klass.getSuperClassMirror().getDeclaredField("jzfile").getLong(jarFileMirror);
        JarFile hostJarFile = (JarFile)getVM().getZipFileForAddress(jzfile);
        
        String[] result = (String[]) getMetaInfEntryNamesMethod.invoke(hostJarFile);
        
        ObjectArrayMirror resultMirror = (ObjectArrayMirror)vm.findBootstrapClassMirror(String.class.getName()).newArray(result.length);
        for (int i = 0; i < result.length; i++) {
            resultMirror.set(i, Reflection.makeString(vm, result[i]));
        }
        
        return ObjectMirage.make(resultMirror);
    }
    

}
