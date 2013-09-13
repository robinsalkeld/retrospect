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

import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.holograms.ArrayHologram;

public class LongArrayHologram extends ArrayHologram implements LongArrayMirror {

    protected final LongArrayMirror mirror;
    
    public LongArrayHologram(LongArrayMirror mirror) {
        super(mirror);
        this.mirror = mirror;
    }

    @Override
    public long getLong(int index) throws ArrayIndexOutOfBoundsException {
        return mirror.getLong(index);
    }

    @Override
    public void setLong(int index, long b) throws ArrayIndexOutOfBoundsException {
        mirror.setLong(index, b);
    }
    
    public static long getHologram(LongArrayMirror mirror, int index) throws Throwable {
        try {
            return mirror.getLong(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
    
    public static void setHologram(LongArrayMirror mirror, int index, long l) throws Throwable {
        try {
            mirror.setLong(index, l);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        } catch (ArrayStoreException e) {
            throw throwableAsHologram(mirror.getClassMirror().getVM(), e);
        }
    }
}
