package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class FileInputStreamStubs extends NativeStubs {

    public FileInputStreamStubs(ClassHolograph klass) {
	super(klass);
    }

    @StubMethod
    public void open(InstanceMirror fis, InstanceMirror name) throws FileNotFoundException, IllegalAccessException, NoSuchFieldException {
        VirtualMachineHolograph vm = klass.getVM();
        
        InstanceMirror fdMirror = (InstanceMirror)HolographInternalUtils.getField(fis, "fd");
        int fd = fdMirror.getInt(fdMirror.getClassMirror().getDeclaredField("fd"));
        
        String realName = Reflection.getRealStringForMirror(name);
        File mappedPath = vm.getMappedFile(new File(realName), true);
        FileInputStream hostFIS = new FileInputStream(mappedPath);
        
        vm.fileInputStreams.put(fd, hostFIS);
    }
    
    private FileInputStream getHostFIS(InstanceMirror fis) throws IllegalAccessException, NoSuchFieldException {
        InstanceMirror fdMirror = (InstanceMirror)HolographInternalUtils.getField(fis, "fd");
        int fd = fdMirror.getInt(fdMirror.getClassMirror().getDeclaredField("fd"));
        
        return klass.getVM().fileInputStreams.get(fd);
    }
    
    @StubMethod
    public void close0(InstanceMirror fis) throws IllegalAccessException, NoSuchFieldException, IOException {
        FileInputStream hostFIS = getHostFIS(fis);
        hostFIS.close();
    }
    
    @StubMethod
    public long skip(InstanceMirror fis, long n) throws IllegalAccessException, NoSuchFieldException, IOException {
        FileInputStream hostFIS = getHostFIS(fis);
        return hostFIS.skip(n);
    }
    
    @StubMethod
    public int readBytes(InstanceMirror fis, ByteArrayMirror b, int off, int len) throws IllegalAccessException, NoSuchFieldException, IOException {
        FileInputStream hostFIS = getHostFIS(fis);
        
        byte[] buffer = new byte[len];
        int result = hostFIS.read(buffer, 0, buffer.length);
        if (result >= 0) {
            Reflection.arraycopy(new NativeByteArrayMirror(buffer), 0, b, off, result);
        }
        return result;
    }
    
    @StubMethod
    public int available(InstanceMirror fis) throws IllegalAccessException, NoSuchFieldException, IOException {
	FileInputStream hostFIS = getHostFIS(fis);
        return hostFIS.available();
    }
}
