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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingInstanceMirror;

public class InstanceHolograph extends WrappingInstanceMirror {

    protected final VirtualMachineHolograph vm;
    private Map<FieldMirror, Object> newValues;
    
    public InstanceHolograph(VirtualMachineHolograph vm, InstanceMirror wrappedInstance) {
        super(vm, wrappedInstance);
        this.vm = vm;

        if (!vm.getWrappedVM().canBeModified() || wrapped instanceof NewInstanceMirror) {
            this.newValues = new HashMap<FieldMirror, Object>();
        } else {
            this.newValues = Collections.emptyMap();
        }
    }
    
    public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (ObjectMirror)newValues.get(field);
        } else {
            return super.get(field);
        }
    }
    
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Boolean)newValues.get(field);
        } else {
            return super.getBoolean(field);
        }
    }
    
    public byte getByte(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Byte)newValues.get(field);
        } else {
            return super.getByte(field);
        }
    }
    
    public char getChar(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Character)newValues.get(field);
        } else {
            return super.getChar(field);
        }
    }
    
    public short getShort(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Short)newValues.get(field);
        } else {
            return super.getShort(field);
        }
    }
    
    public int getInt(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Integer)newValues.get(field);
        } else {
            return super.getInt(field);
        }
    }
    
    public long getLong(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Long)newValues.get(field);
        } else {
            return super.getLong(field);
        }
    }
    
    public float getFloat(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Float)newValues.get(field);
        } else {
            return super.getFloat(field);
        }
    }
    
    public double getDouble(FieldMirror field) throws IllegalAccessException {
        if (newValues.containsKey(field)) {
            return (Double)newValues.get(field);
        } else {
            return super.getDouble(field);
        }
    }
    
    private void checkForIllegalMutation(FieldMirror field) {
        if (!VirtualMachineHolograph.UNSAFE_MODE && !(wrapped instanceof NewInstanceMirror)) {
            String message = "Illegal set to field " + 
                    field.getDeclaringClass().getClassName() + "." + field.getName();
            System.err.println(message);
//            throw new InternalError(message);
        }
    }

    public void set(FieldMirror field, ObjectMirror o) {
        checkForIllegalMutation(field);
        newValues.put(field, o);
    }
    
    public void setBoolean(FieldMirror field, boolean b) {
        checkForIllegalMutation(field);
        newValues.put(field, b);
    }
    
    public void setByte(FieldMirror field, byte b) {
        checkForIllegalMutation(field);
        newValues.put(field, b);
    }
    
    public void setChar(FieldMirror field, char c) {
        checkForIllegalMutation(field);
        newValues.put(field, c);
    }
    
    public void setShort(FieldMirror field, short s) {
        checkForIllegalMutation(field);
        newValues.put(field, s);
    }
    
    public void setInt(FieldMirror field, int i) {
        checkForIllegalMutation(field);
        newValues.put(field, i);
    }
    
    public void setLong(FieldMirror field, long l) {
        checkForIllegalMutation(field);
        newValues.put(field, l);
    }
    
    public void setFloat(FieldMirror field, float f) {
        checkForIllegalMutation(field);
        newValues.put(field, f);
    }
    
    public void setDouble(FieldMirror field, double d) {
        checkForIllegalMutation(field);
        newValues.put(field, d);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 3);
        return sb.toString();
    }
    
    public void toString(StringBuilder sb, int maxDepth) {
        sb.append(getClass().getSimpleName() + "[" + getClassMirror().getClassName() + "]@" + System.identityHashCode(this) + "(");
        if (maxDepth == 0) {
            sb.append("...");
        } else {
            boolean first = true;
            for (Map.Entry<FieldMirror, Object> entry : newValues.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(entry.getKey().getName());
                sb.append("=");
                Object value = entry.getValue();
                if (value instanceof InstanceHolograph) {
                    ((InstanceHolograph)value).toString(sb, maxDepth - 1);
                } else {
                    sb.append(value);
                }
            }
        }
        sb.append(")");
    }
}
