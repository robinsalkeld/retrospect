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
}
