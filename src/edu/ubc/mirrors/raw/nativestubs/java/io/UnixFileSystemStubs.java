package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.File;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;

public class UnixFileSystemStubs {

    public static long getLastModifiedTime(Class<?> classLoaderLiteral, Mirage fs, Mirage f) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        File mappedFile = vm.getMappedFile((InstanceMirror)f.getMirror());
        return mappedFile.lastModified();
    }
    
    
}
