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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class RandomAccessFileStubs extends NativeStubs {

    private Map<InstanceMirror, RandomAccessFile> hostFiles = 
            new HashMap<InstanceMirror, RandomAccessFile>();
    
    public RandomAccessFileStubs(ClassHolograph klass) {
        super(klass);
    }

    @StubMethod
    public void seek(InstanceMirror file, long position) throws IOException {
        RandomAccessFile hostFile = getHostFile(file);
        hostFile.seek(position);
    }

    @StubMethod
    public int readBytes(InstanceMirror file, ByteArrayMirror b, int off, int len) throws IOException {
        RandomAccessFile hostFile = getHostFile(file);
        byte[] buffer = new byte[len];
        int result = hostFile.read(buffer, 0, buffer.length);
        if (result >= 0) {
            Reflection.arraycopy(new NativeByteArrayMirror(buffer), 0, b, off, result);
        }
        return result;
    }

    private RandomAccessFile getHostFile(InstanceMirror file) throws FileNotFoundException {
        RandomAccessFile hostFile = hostFiles.get(file);
        if (hostFile == null) {
            String path = findRAFPath(file);
            hostFile = new RandomAccessFile(new File(path), "r");
            hostFiles.put(file, hostFile);
        }
        return hostFile;
    }
    
    private String findRAFPath(InstanceMirror file) {
        // The actual path is only stored in native code,
        // so we need to find it elsewhere.
        ClassMirror brisClass = getVM().findAllClasses("org.eclipse.core.internal.registry.BufferedRandomInputStream", false).iterator().next();
        FieldMirror inputFileField = brisClass.getDeclaredField("inputFile");
        FieldMirror filePathField = brisClass.getDeclaredField("filePath");
        for (ObjectMirror bris : brisClass.getInstances()) {
            InstanceMirror brisInstance = (InstanceMirror)bris;
            try {
                ObjectMirror raf = brisInstance.get(inputFileField);
                if (raf.equals(file)) {
                    return Reflection.getRealStringForMirror((InstanceMirror)brisInstance.get(filePathField));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } 
        }
        
        throw new IllegalStateException("Couldn't find RandomAccessFile path");
    }
}
