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
package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;

import edu.ubc.mirrors.BlankInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.HolographInternalUtils;


public class ConstantPoolReader extends BlankInstanceMirror implements InstanceMirror {

    private final ClassMirror klass;
    private final ClassReader reader;
    private final char[] buf;
    
    private static final Method readUTFMethod;
    static {
        try {
            readUTFMethod = ClassReader.class.getDeclaredMethod("readUTF", Integer.TYPE, Integer.TYPE, char[].class);
            readUTFMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ConstantPoolReader(ClassMirror klass) {
        this.klass = klass;
        this.reader = new ClassReader(klass.getBytecode());
        this.buf = new char[reader.getMaxStringLength()];
    }
    
    public int getSize() {
        return reader.getItemCount();
    }

    public ClassMirror getClassAt(int index) {
        String name = reader.readClass(reader.getItem(index), buf);
        return HolographInternalUtils.loadClassMirrorInternal(klass, Type.getObjectType(name).getClassName());
    }
    
    public InstanceMirror getStringAt(int index) {
        int offset = reader.getItem(index);
        String s;
        try {
            s = (String)readUTFMethod.invoke(reader, offset + 2, reader.readUnsignedShort(offset), buf);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return klass.getVM().makeString(s);
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return klass.getVM().findBootstrapClassMirror(Object.class.getName());
    }
    
    @Override
    public boolean canLock() {
        return true;
    }
}
