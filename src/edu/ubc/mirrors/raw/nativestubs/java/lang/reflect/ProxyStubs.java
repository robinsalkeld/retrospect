package edu.ubc.mirrors.raw.nativestubs.java.lang.reflect;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.nativestubs.java.lang.ClassLoaderStubs;

public class ProxyStubs {

    public static Mirage defineClass0(Class<?> classLoaderLiteral, Mirage loader, Mirage name, Mirage b, int off, int len) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        String realClassName = Reflection.getRealStringForMirror((InstanceMirror)name.getMirror());
        
        if (loader == null) {
            return ObjectMirage.make(vm.defineBootstrapClass(realClassName, (ByteArrayMirror)b.getMirror(), off, len));
        } else {
            Mirage className = ObjectMirage.make(Reflection.makeString(vm, realClassName));
            return ClassLoaderStubs.defineClass1(classLoaderLiteral, loader, className, b, off, len, null, null);
        }
    }
}
