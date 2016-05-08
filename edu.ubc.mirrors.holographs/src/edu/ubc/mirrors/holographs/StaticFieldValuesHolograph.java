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

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.StaticFieldValuesMirror;

public class StaticFieldValuesHolograph extends InstanceHolograph implements StaticFieldValuesMirror {

    private final ClassHolograph klass;
    
    public StaticFieldValuesHolograph(VirtualMachineHolograph vm, StaticFieldValuesMirror wrapped) {
        super(vm, wrapped);
        this.klass = (ClassHolograph)vm.getWrappedMirror(wrapped.forClassMirror());
    }
    
    @Override
    public ClassHolograph forClassMirror() {
        return klass;
    }
    
    @Override
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        klass.ensureInitialized();
        return super.get(field);
    }
    
    @Override
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        klass.ensureInitialized();
        return super.getBoolean(field);
    }
    
    @Override
    public byte getByte(FieldMirror field) throws IllegalAccessException {
        klass.ensureInitialized();
        return super.getByte(field);
    }
    
    @Override
    public char getChar(FieldMirror field) throws IllegalAccessException {
        klass.ensureInitialized();
        return super.getChar(field);
    }
    
    @Override
    public short getShort(FieldMirror field) throws IllegalAccessException {
        klass.ensureInitialized();
        return super.getShort(field);
    }
    
    @Override
    public int getInt(FieldMirror field) throws IllegalAccessException {
        klass.ensureInitialized();
        return super.getInt(field);
    }
    
    @Override
    public long getLong(FieldMirror field) throws IllegalAccessException {
        klass.ensureInitialized();
        return super.getLong(field);
    }
    
    @Override
    public float getFloat(FieldMirror field) throws IllegalAccessException {
        klass.ensureInitialized();
        return super.getFloat(field);
    }
    
    @Override
    public double getDouble(FieldMirror field) throws IllegalAccessException {
        klass.ensureInitialized();
        return super.getDouble(field);
    }
    
    @Override
    public void set(FieldMirror field, ObjectMirror o) {
        klass.ensureInitialized();
        super.set(field, o);
    }
    
    @Override
    public void setBoolean(FieldMirror field, boolean b) {
        klass.ensureInitialized();
        super.setBoolean(field, b);
    }
    
    @Override
    public void setByte(FieldMirror field, byte b) {
        klass.ensureInitialized();
        super.setByte(field, b);
    }
    
    @Override
    public void setChar(FieldMirror field, char c) {
        klass.ensureInitialized();
        super.setChar(field, c);
    }
    
    @Override
    public void setShort(FieldMirror field, short s) {
        klass.ensureInitialized();
        super.setShort(field, s);
    }
    
    @Override
    public void setInt(FieldMirror field, int i) {
        klass.ensureInitialized();
        super.setInt(field, i);
    }
    
    @Override
    public void setLong(FieldMirror field, long l) {
        klass.ensureInitialized();
        super.setLong(field, l);
    }
    
    @Override
    public void setFloat(FieldMirror field, float f) {
        klass.ensureInitialized();
        super.setFloat(field, f);
    }
    
    @Override
    public void setDouble(FieldMirror field, double d) {
        klass.ensureInitialized();
        super.setDouble(field, d);
    }
}
