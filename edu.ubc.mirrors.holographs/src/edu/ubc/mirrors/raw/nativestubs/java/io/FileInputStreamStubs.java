package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.HolographInternalUtils;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;

public class FileInputStreamStubs extends NativeStubs {

    public FileInputStreamStubs(ClassHolograph klass) {
	super(klass);
    }

    public void open(Mirage fis, Mirage name) throws FileNotFoundException, IllegalAccessException, NoSuchFieldException {
        VirtualMachineHolograph vm = klass.getVM();
        
        InstanceMirror fisMirror = (InstanceMirror)fis.getMirror();
        InstanceMirror fdMirror = (InstanceMirror)HolographInternalUtils.getField(fisMirror, "fd");
        int fd = fdMirror.getInt(fdMirror.getClassMirror().getDeclaredField("fd"));
        
        String realName = Reflection.getRealStringForMirror((InstanceMirror)name.getMirror());
        File mappedPath = vm.getMappedFile(new File(realName), true);
        FileInputStream hostFIS = new FileInputStream(mappedPath);
        
        vm.fileInputStreams.put(fd, hostFIS);
    }
    
    private FileInputStream getHostFIS(Mirage fis) throws IllegalAccessException, NoSuchFieldException {
        InstanceMirror fisMirror = (InstanceMirror)fis.getMirror();
        InstanceMirror fdMirror = (InstanceMirror)HolographInternalUtils.getField(fisMirror, "fd");
        int fd = fdMirror.getInt(fdMirror.getClassMirror().getDeclaredField("fd"));
        
        return klass.getVM().fileInputStreams.get(fd);
    }
    
    public void close0(Mirage fis) throws IllegalAccessException, NoSuchFieldException, IOException {
        FileInputStream hostFIS = getHostFIS(fis);
        hostFIS.close();
    }
    
    public int readBytes(Mirage fis, Mirage b, int off, int len) throws IllegalAccessException, NoSuchFieldException, IOException {
        FileInputStream hostFIS = getHostFIS(fis);
        
        byte[] buffer = new byte[len];
        int result = hostFIS.read(buffer, 0, buffer.length);
        if (result >= 0) {
            ByteArrayMirror bMirror = (ByteArrayMirror)b.getMirror();
            SystemStubs.arraycopyMirrors(new NativeByteArrayMirror(buffer), 0, bMirror, off, result);
        }
        return result;
    }
    
    public int available(Mirage fis) throws IllegalAccessException, NoSuchFieldException, IOException {
	FileInputStream hostFIS = getHostFIS(fis);
        return hostFIS.available();
    }
}
