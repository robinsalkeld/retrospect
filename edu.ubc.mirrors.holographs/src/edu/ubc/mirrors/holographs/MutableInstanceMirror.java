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
package edu.ubc.mirrors.holographs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;

public class MutableInstanceMirror implements InstanceMirror {
    
    private final InstanceMirror wrapped;
    private final Map<FieldMirror, Object> newValues = new HashMap<FieldMirror, Object>();
    
    public MutableInstanceMirror(InstanceMirror wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ClassMirror getClassMirror() {
        return wrapped.getClassMirror();
    }
    
    @Override
    public int identityHashCode() {
        return wrapped.identityHashCode();
    }
    
    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (ObjectMirror)newValues.get(field);
        } else {
            return wrapped.get(field);
        }
    }
    
    @Override
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Boolean)newValues.get(field);
        } else {
            return wrapped.getBoolean(field);
        }
    }
    
    @Override
    public byte getByte(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Byte)newValues.get(field);
        } else {
            return wrapped.getByte(field);
        }
    }
    
    @Override
    public char getChar(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Character)newValues.get(field);
        } else {
            return wrapped.getChar(field);
        }
    }
    
    @Override
    public short getShort(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Short)newValues.get(field);
        } else {
            return wrapped.getShort(field);
        }
    }
    
    @Override
    public int getInt(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Integer)newValues.get(field);
        } else {
            return wrapped.getInt(field);
        }
    }
    
    @Override
    public long getLong(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Long)newValues.get(field);
        } else {
            return wrapped.getLong(field);
        }
    }
    
    @Override
    public float getFloat(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Float)newValues.get(field);
        } else {
            return wrapped.getFloat(field);
        }
    }
    
    @Override
    public double getDouble(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Double)newValues.get(field);
        } else {
            return wrapped.getDouble(field);
        }
    }
    
    @Override
    public void set(FieldMirror field, ObjectMirror o) {
        newValues.put(field, o);
    }
    
    @Override
    public void setBoolean(FieldMirror field, boolean b) {
        newValues.put(field, b);
    }
    
    @Override
    public void setByte(FieldMirror field, byte b) {
        newValues.put(field, b);
    }
    
    @Override
    public void setChar(FieldMirror field, char c) {
        newValues.put(field, c);
    }
    
    @Override
    public void setShort(FieldMirror field, short s) {
        newValues.put(field, s);
    }
    
    @Override
    public void setInt(FieldMirror field, int i) {
        newValues.put(field, i);
    }
    
    @Override
    public void setLong(FieldMirror field, long l) {
        newValues.put(field, l);
    }
    
    @Override
    public void setFloat(FieldMirror field, float f) {
        newValues.put(field, f);
    }
    
    @Override
    public void setDouble(FieldMirror field, double d) {
        newValues.put(field, d);
    }

    public Set<FieldMirror> modifiedFields() {
        return newValues.keySet();
    }
    
    @Override
    public void allowCollection(boolean flag) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isCollected() {
        throw new UnsupportedOperationException();
    }
}
