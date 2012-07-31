package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.File;
import java.io.IOException;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class UnixFileSystemStubs {

    public static long getLastModifiedTime(Class<?> classLoaderLiteral, Mirage fs, Mirage f) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        File mappedFile = vm.getMappedFile((InstanceMirror)f.getMirror());
        return mappedFile.lastModified();
    }
    
    // TODO-RS: This should actually do the work itself so we could theoretically
    // emulate across platforms
    public static Mirage canonicalize0(Class<?> classLoaderLiteral, Mirage fs, Mirage f) throws IOException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        String path = Reflection.getRealStringForMirror((InstanceMirror)f.getMirror());
        String result = new File(path).getCanonicalPath();
        return ObjectMirage.make(Reflection.makeString(vm, result));
    }
}
