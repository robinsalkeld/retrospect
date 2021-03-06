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

import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.holograms.ArrayHologram;

public class IntArrayHologram extends ArrayHologram implements IntArrayMirror {

    protected final IntArrayMirror mirror;
    
    public IntArrayHologram(IntArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public int getInt(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getInt(index);
    }

    @Override
    public int[] getInts(int index, int length) throws ArrayIndexOutOfBoundsException {
        return mirror.getInts(index, length);
    }
    
    @Override
    public void setInt(int index, int b) throws ArrayIndexOutOfBoundsException {
        mirror.setInt(index, b);
    }
    
    @Override
    public void setInts(int index, int[] b) throws ArrayIndexOutOfBoundsException {
        mirror.setInts(index, b);
    }
    
    public static int getHologram(IntArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getInt(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(IntArrayMirror mirror, int index, int b) throws Throwable {
        try {
            mirror.setInt(index, b);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
