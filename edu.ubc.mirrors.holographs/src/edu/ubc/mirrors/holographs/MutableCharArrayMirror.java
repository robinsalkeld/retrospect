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

import java.util.Arrays;
import java.util.BitSet;

import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingCharArrayMirror;

public class MutableCharArrayMirror extends WrappingCharArrayMirror {

    private final VirtualMachineHolograph vm;
    private final CharArrayMirror immutableMirror;
    
    private char[] cached;
    private long mark = -1;
    
    private char[] newValues;
    private final BitSet overwritten = new BitSet();
    
    public MutableCharArrayMirror(VirtualMachineHolograph vm, CharArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.vm = vm;
        this.immutableMirror = immutableMirror;
    }
    
    private void checkCache() {
        long currentMark = vm.eventQueue().getEventsCount();
        if (mark != currentMark){
            cached = immutableMirror.getChars(0, immutableMirror.length());
            mark = currentMark;
        }     
    }
    
    
    @Override
    public char getChar(int index) throws ArrayIndexOutOfBoundsException {
        if (overwritten.get(index)) {
            return newValues[index];
        }
        
        checkCache();
        return cached[index];
    }

    @Override
    public char[] getChars(int index, int length) throws ArrayIndexOutOfBoundsException {
        if (length == 0) {
             return new char[0];
        }
        
        int end = index + length;
        
        // All overwritten
        if (overwritten.nextClearBit(index) >= end) {
            return Arrays.copyOfRange(newValues, index, end);
        }
                
        // Nothing overwritten
        int firstOverwritten = overwritten.nextSetBit(index);
        if (firstOverwritten >= end || firstOverwritten == -1) {
            checkCache();
            return Arrays.copyOfRange(cached, index, end);
        }
        
        // General case - some overwritten
        return super.getChars(index, length);
    }
    
    private void initNewValues() {
        if (newValues == null) {
            this.newValues = new char[length()];
        }
    }
    
    @Override
    public void setChar(int index, char b) throws ArrayIndexOutOfBoundsException {
        vm.checkForIllegalMutation(wrapped);
        
        initNewValues();
        newValues[index] = b;
        overwritten.set(index);
    }

    @Override
    public void setChars(int index, char[] b) throws ArrayIndexOutOfBoundsException {
        vm.checkForIllegalMutation(wrapped);
        
        initNewValues();
        System.arraycopy(b, 0, newValues, index, b.length);
        overwritten.set(index, index + b.length);
    }
}
