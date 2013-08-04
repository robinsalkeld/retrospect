package edu.ubc.mirrors.raw.nativestubs.java.util.zip;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipFile;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class ZipEntryStubs extends ZipFileStubs {

    public ZipEntryStubs(ClassHolograph klass) {
        super(klass);
    }

    @StubMethod
    public void initFields(InstanceMirror zipEntry, long jzentry) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, UnsupportedEncodingException {
        zipEntry.setLong(klass.getDeclaredField("time"), getEntryTime(jzentry));
        zipEntry.setLong(klass.getDeclaredField("crc"), getEntryCrc(jzentry));
        zipEntry.setLong(klass.getDeclaredField("size"), getEntrySize(jzentry));
        zipEntry.setLong(klass.getDeclaredField("csize"), getEntryCSize(jzentry));
        zipEntry.setInt(klass.getDeclaredField("method"), getEntryMethod(jzentry));

        zipEntry.set(klass.getDeclaredField("extra"), getEntryBytes(jzentry, 1));
        
        byte[] commentBytes = (byte[])getHostNativeMethod(ZipFile.class, "getEntryBytes", Long.TYPE, Integer.TYPE).invoke(null, jzentry, 2);
        if (commentBytes != null) {
            String commentString = new String(commentBytes, 0, commentBytes.length, "UTF-8");
            InstanceMirror commentStringMirror = Reflection.makeString(getVM(), commentString);
            zipEntry.set(klass.getDeclaredField("comment"), commentStringMirror);
        }
    }
}
