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
import java.io.IOException;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

//TODO-RS: These should actually do the work themselves so we could theoretically
//emulate across platforms
public class FileSystemStubs extends NativeStubs {

    public FileSystemStubs(ClassHolograph klass) {
        super(klass);
    }

    private File getMappedFile(InstanceMirror f, boolean errorOnUnmapped) {
        return klass.getVM().getMappedFile(f, errorOnUnmapped);
    }
    
    @StubMethod
    public long getLastModifiedTime(InstanceMirror fs, InstanceMirror f) {
        File mappedFile = getMappedFile(f, false);
        return mappedFile != null ? mappedFile.lastModified() : 0;
    }
    
    @StubMethod
    public InstanceMirror canonicalize0(InstanceMirror fs, InstanceMirror f) throws IOException {
        String path = Reflection.getRealStringForMirror(f);
        String result = new File(path).getCanonicalPath();
        return Reflection.makeString(klass.getVM(), result);
    }
    
    @StubMethod
    public int getBooleanAttributes0(InstanceMirror fs, InstanceMirror f) {
        File mappedFile = getMappedFile(f, false);
        int result = 0;
        if (mappedFile == null) {
            return result;
        }
        
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
    
    @StubMethod
    public long getLength(InstanceMirror fs, InstanceMirror f) {
        File mappedFile = getMappedFile(f, false);
        return mappedFile.length();
    }
    
    @StubMethod
    public boolean checkAccess(InstanceMirror fs, InstanceMirror f, int access) {
        File mappedFile = getMappedFile(f, false);
        if (access == 0x04) {
            return mappedFile.canRead();
        }
        if (access == 0x02) {
            return mappedFile.canWrite();
        }
        if (access == 0x01) {
            return mappedFile.canExecute();
        }
        return false;
    }
}
