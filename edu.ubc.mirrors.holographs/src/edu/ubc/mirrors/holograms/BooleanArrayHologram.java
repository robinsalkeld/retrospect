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
package edu.ubc.mirrors.holograms;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;

/**
 * Note that this class has to implement ByteArrayMirror as well since boolean arrays are actually
 * accessed with the BALOAD and BASTORE opcodes.
 * 
 * @author robinsalkeld
 */
public class BooleanArrayHologram extends ArrayHologram implements BooleanArrayMirror {

    protected final BooleanArrayMirror mirror;
    
    public BooleanArrayHologram(BooleanArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getBoolean(index);
    }

    @Override
    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        mirror.setBoolean(index, b);
    }

    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getBoolean(index) ? (byte)1 : (byte)0;
    }

    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        mirror.setBoolean(index, b != 0);
    }
    
    public static boolean getHologram(BooleanArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getBoolean(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(BooleanArrayMirror mirror, int index, boolean b) throws Throwable {
        try {
            mirror.setBoolean(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static byte getHologram(ByteArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getByte(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(ByteArrayMirror mirror, int index, byte b) throws Throwable {
        try {
            mirror.setByte(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
