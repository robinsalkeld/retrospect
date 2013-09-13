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

import java.lang.reflect.Field;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;


public class NativeInstanceMirror extends NativeObjectMirror implements InstanceMirror {

    private final Object object;
    
    public NativeInstanceMirror(Object object) {
        super(object);
        this.object = object;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NativeInstanceMirror)) {
            return false;
        }
        
        return object == ((NativeInstanceMirror)obj).object;
    }
    
    @Override
    public int hashCode() {
        return 17 + System.identityHashCode(object);
    }
    
    public static NativeInstanceMirror make(Class<?> nativeClass) {
        try {
            return new NativeInstanceMirror(nativeClass.newInstance());
        } catch (IllegalAccessException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        } catch (InstantiationException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        }
    }
    
    public ClassMirror getClassMirror() {
        return new NativeClassMirror(object.getClass());
    }
    
    public static class NativeFieldMirror implements FieldMirror {
        
        final Field field;
        
        public NativeFieldMirror(Field field) {
            this.field = field;
            field.setAccessible(true);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NativeFieldMirror)) {
                return false;
            }
            
            NativeFieldMirror other = (NativeFieldMirror)obj;
            return field.equals(other.field);
        }
        
        @Override
        public int hashCode() {
            return field.hashCode();
        }
        
        @Override
        public ClassMirror getDeclaringClass() {
            return (ClassMirror)NativeInstanceMirror.makeMirror(field.getDeclaringClass());
        }
        
        @Override
        public String getName() {
            return field.getName();
        }
        
        @Override
        public String getTypeName() {
            return field.getType().getName();
        }
        
        @Override
        public ClassMirror getType() {
            return (ClassMirror)NativeInstanceMirror.makeMirror(field.getType());
        }
        
        @Override
        public int getModifiers() {
            return field.getModifiers();
        }
    }
    private Field getNativeField(FieldMirror field) {
        return ((NativeFieldMirror)field).field;
    }
    
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        Object nativeValue = getNativeField(field).get(object);
        return makeMirror(nativeValue);
    }

    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        return getNativeField(field).getBoolean(object);
    }

    public byte getByte(FieldMirror field) throws IllegalAccessException {
        return getNativeField(field).getByte(object);
    }

    public char getChar(FieldMirror field) throws IllegalAccessException {
        return getNativeField(field).getChar(object);
    }

    public short getShort(FieldMirror field) throws IllegalAccessException {
        return getNativeField(field).getShort(object);
    }

    public int getInt(FieldMirror field) throws IllegalAccessException {
        return getNativeField(field).getInt(object);
    }

    public long getLong(FieldMirror field) throws IllegalAccessException {
        return getNativeField(field).getLong(object);
    }

    public float getFloat(FieldMirror field) throws IllegalAccessException {
        return getNativeField(field).getFloat(object);
    }

    public double getDouble(FieldMirror field) throws IllegalAccessException {
        return getNativeField(field).getDouble(object);
    }

    public void set(FieldMirror field, ObjectMirror o) throws IllegalAccessException {
        getNativeField(field).set(object, ((NativeInstanceMirror)o).object);
    }

    public void setBoolean(FieldMirror field, boolean b) throws IllegalAccessException {
        getNativeField(field).setBoolean(object, b);
    }

    public void setByte(FieldMirror field, byte b) throws IllegalAccessException {
        getNativeField(field).setByte(object, b);
    }

    public void setChar(FieldMirror field, char c) throws IllegalAccessException {
        getNativeField(field).setChar(object, c);
    }

    public void setShort(FieldMirror field, short s) throws IllegalAccessException {
        getNativeField(field).setShort(object, s);
    }

    public void setInt(FieldMirror field, int i) throws IllegalAccessException {
        getNativeField(field).setInt(object, i);
    }

    public void setLong(FieldMirror field, long l) throws IllegalAccessException {
        getNativeField(field).setLong(object, l);
    }

    public void setFloat(FieldMirror field, float f) throws IllegalAccessException {
        getNativeField(field).setFloat(object, f);
    }

    public void setDouble(FieldMirror field, double d) throws IllegalAccessException {
        getNativeField(field).setDouble(object, d);
    }
    
    public static ObjectMirror makeMirror(Object object) {
        if (object == null) {
            return null;
        }
        
        if (object instanceof Class) {
            return new NativeClassMirror((Class<?>)object);
        } else if (object instanceof ClassLoader) {
            return new NativeClassMirrorLoader((ClassLoader)object);
        } else if (object instanceof Thread) {
            return new NativeThreadMirror((Thread)object);
        } else if (object instanceof Object[]) {
            return new NativeObjectArrayMirror((Object[])object);
        } else if (object instanceof boolean[]) {
            return new NativeBooleanArrayMirror((boolean[])object);
        } else if (object instanceof byte[]) {
            return new NativeByteArrayMirror((byte[])object);
        } else if (object instanceof char[]) {
            return new NativeCharArrayMirror((char[])object);
        } else if (object instanceof short[]) {
            return new NativeShortArrayMirror((short[])object);
        } else if (object instanceof int[]) {
            return new NativeIntArrayMirror((int[])object);
        } else if (object instanceof long[]) {
            return new NativeLongArrayMirror((long[])object);
        } else if (object instanceof float[]) {
            return new NativeFloatArrayMirror((float[])object);
        } else if (object instanceof double[]) {
            return new NativeDoubleArrayMirror((double[])object);
        } else {
            return new NativeInstanceMirror(object);
        }
    }
    
    @Override
    public String toString() {
        return "NativeObjectMirror: " + object;
    }
}
