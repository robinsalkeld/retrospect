package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import sun.management.FileSystem;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

// TODO-RS: These should actually do the work themselves so we could theoretically
// emulate across platforms
public class UnixFileSystemStubs {

    private static File getMappedFile(Class<?> classLoaderLiteral, Mirage f) {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        return vm.getMappedFile((InstanceMirror)f.getMirror());
    }
    
    public static long getLastModifiedTime(Class<?> classLoaderLiteral, Mirage fs, Mirage f) {
        File mappedFile = getMappedFile(classLoaderLiteral, f);
        return mappedFile.lastModified();
    }
    
    public static Mirage canonicalize0(Class<?> classLoaderLiteral, Mirage fs, Mirage f) throws IOException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        String path = Reflection.getRealStringForMirror((InstanceMirror)f.getMirror());
        String result = new File(path).getCanonicalPath();
        return ObjectMirage.make(Reflection.makeString(vm, result));
    }
    
    public static int getBooleanAttributes0(Class<?> classLoaderLiteral, Mirage fs, Mirage f) {
        File mappedFile = getMappedFile(classLoaderLiteral, f);
        int result = 0;
        if (mappedFile.exists()) {
            result |= 0x01;
        }
        if (mappedFile.isFile()) {
            result |= 0x02;
        }
        if (mappedFile.isDirectory()) {
            result |= 0x04;
        }
        if (mappedFile.isHidden()) {
            result |= 0x08;
        }
        return result;
    }
}
