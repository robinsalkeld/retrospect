package edu.ubc.mirrors.raw.nativestubs.java.util.jar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarFile;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

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
    
    @StubMethod
    public ObjectArrayMirror getMetaInfEntryNames(InstanceMirror jarFileMirror) throws IllegalAccessException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException {
        VirtualMachineHolograph vm = getVM();
        
        long jzfile = jarFileMirror.getLong(klass.getSuperClassMirror().getDeclaredField("jzfile"));
        JarFile hostJarFile = (JarFile)getVM().getZipFileForAddress(jzfile);
        
        String[] result = (String[]) getMetaInfEntryNamesMethod.invoke(hostJarFile);
        
        ObjectArrayMirror resultMirror = (ObjectArrayMirror)vm.findBootstrapClassMirror(String.class.getName()).newArray(result.length);
        for (int i = 0; i < result.length; i++) {
            resultMirror.set(i, Reflection.makeString(vm, result[i]));
        }
        
        return resultMirror;
    }
    

}
