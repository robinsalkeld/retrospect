package edu.ubc.mirrors.raw.nativestubs.java.io;

import java.io.File;
import java.io.IOException;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

// TODO-RS: These should actually do the work themselves so we could theoretically
// emulate across platforms
public class UnixFileSystemStubs extends NativeStubs {

    public UnixFileSystemStubs(ClassHolograph klass) {
	super(klass);
    }

    private File getMappedFile(Mirage f, boolean errorOnUnmapped) {
        return klass.getVM().getMappedFile((InstanceMirror)f.getMirror(), errorOnUnmapped);
    }
    
    public long getLastModifiedTime(Mirage fs, Mirage f) {
        File mappedFile = getMappedFile(f, false);
        return mappedFile != null ? mappedFile.lastModified() : 0;
    }
    
    public Mirage canonicalize0(Mirage fs, Mirage f) throws IOException {
        String path = Reflection.getRealStringForMirror((InstanceMirror)f.getMirror());
        String result = new File(path).getCanonicalPath();
        return ObjectMirage.make(Reflection.makeString(klass.getVM(), result));
    }
    
    public int getBooleanAttributes0(Mirage fs, Mirage f) {
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
    
    public long getLength(Mirage fs, Mirage f) {
        File mappedFile = getMappedFile(f, false);
        return mappedFile.length();
    }
}
