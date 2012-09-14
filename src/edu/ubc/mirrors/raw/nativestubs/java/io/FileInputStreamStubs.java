package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class FileInputStreamStubs {

    public static void open(Class<?> classLoaderLiteral, Mirage fis, Mirage name) throws FileNotFoundException, IllegalAccessException, NoSuchFieldException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        InstanceMirror fisMirror = (InstanceMirror)fis.getMirror();
        InstanceMirror fdMirror = (InstanceMirror)Reflection.getField(fisMirror, "fd");
        int fd = fdMirror.getMemberField("fd").getInt();
        
        String realName = Reflection.getRealStringForMirror((InstanceMirror)name.getMirror());
        File mappedPath = vm.getMappedFile(new File(realName), true);
        FileInputStream hostFIS = new FileInputStream(mappedPath);
        
        vm.fileInputStreams.put(fd, hostFIS);
    }
    
    private static FileInputStream getHostFIS(Class<?> classLoaderLiteral, Mirage fis) throws IllegalAccessException, NoSuchFieldException {
        MirageClassLoader callingLoader = (MirageClassLoader)classLoaderLiteral.getClassLoader();
        VirtualMachineHolograph vm = (VirtualMachineHolograph)callingLoader.getVM();
        
        InstanceMirror fisMirror = (InstanceMirror)fis.getMirror();
        InstanceMirror fdMirror = (InstanceMirror)Reflection.getField(fisMirror, "fd");
        int fd = fdMirror.getMemberField("fd").getInt();
        
        return vm.fileInputStreams.get(fd);
    }
    
    public static void close0(Class<?> classLoaderLiteral, Mirage fis) throws IllegalAccessException, NoSuchFieldException, IOException {
        FileInputStream hostFIS = getHostFIS(classLoaderLiteral, fis);
        hostFIS.close();
    }
    
    public static int readBytes(Class<?> classLoaderLiteral, Mirage fis, Mirage b, int off, int len) throws IllegalAccessException, NoSuchFieldException, IOException {
        FileInputStream hostFIS = getHostFIS(classLoaderLiteral, fis);
        
        byte[] buffer = new byte[len];
        int result = hostFIS.read(buffer, 0, buffer.length);
        if (result >= 0) {
            ByteArrayMirror bMirror = (ByteArrayMirror)b.getMirror();
            SystemStubs.arraycopyMirrors(new NativeByteArrayMirror(buffer), 0, bMirror, off, result);
        }
        return result;
    }
}
