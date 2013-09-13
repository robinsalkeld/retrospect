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
package edu.ubc.mirrors;


public interface InstanceMirror extends ObjectMirror {

    public ObjectMirror get(FieldMirror field) throws IllegalAccessException;
    public boolean getBoolean(FieldMirror field) throws IllegalAccessException;
    public byte getByte(FieldMirror field) throws IllegalAccessException;
    public char getChar(FieldMirror field) throws IllegalAccessException;
    public short getShort(FieldMirror field) throws IllegalAccessException;
    public int getInt(FieldMirror field) throws IllegalAccessException;
    public long getLong(FieldMirror field) throws IllegalAccessException;
    public float getFloat(FieldMirror field) throws IllegalAccessException;
    public double getDouble(FieldMirror field) throws IllegalAccessException;
    
    public void set(FieldMirror field, ObjectMirror o) throws IllegalAccessException;
    public void setBoolean(FieldMirror field, boolean b) throws IllegalAccessException;
    public void setByte(FieldMirror field, byte b) throws IllegalAccessException;
    public void setChar(FieldMirror field, char c) throws IllegalAccessException;
    public void setShort(FieldMirror field, short s) throws IllegalAccessException;
    public void setInt(FieldMirror field, int i) throws IllegalAccessException;
    public void setLong(FieldMirror field, long l) throws IllegalAccessException;
    public void setFloat(FieldMirror field, float f) throws IllegalAccessException;
    public void setDouble(FieldMirror field, double d) throws IllegalAccessException;
}
