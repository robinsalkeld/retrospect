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
            InstanceMirror commentStringMirror = getVM().makeString(commentString);
            zipEntry.set(klass.getDeclaredField("comment"), commentStringMirror);
        }
    }
}
