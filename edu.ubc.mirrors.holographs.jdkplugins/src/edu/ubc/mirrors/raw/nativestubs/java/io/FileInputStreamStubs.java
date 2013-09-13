/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
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
