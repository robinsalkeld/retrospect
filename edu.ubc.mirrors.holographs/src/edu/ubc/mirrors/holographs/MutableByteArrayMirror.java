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

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingByteArrayMirror;

public class MutableByteArrayMirror extends WrappingByteArrayMirror {

    private final VirtualMachineHolograph vm;
    private final ByteArrayMirror immutableMirror;
    
    private byte[] cached;
    private long mark = -1;
    
    private byte[] newValues;
    private final BitSet overwritten = new BitSet();
    
    public MutableByteArrayMirror(VirtualMachineHolograph vm, ByteArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.vm = vm;
        this.immutableMirror = immutableMirror;
    }
    
    private void checkCache() {
        long currentMark = vm.eventQueue().getEventsCount();
        if (mark != currentMark){
            cached = immutableMirror.getBytes(0, immutableMirror.length());
            mark = currentMark;
        }     
    }
    
    @Override
    public byte getByte(int index) throws ArrayIndexOutOfBoundsException {
        byte result;
        if (overwritten.get(index)) {
            result = newValues[index];
        } else {
            checkCache();
            result = cached[index];
        }
        return result;
    }

    @Override
    public byte[] getBytes(int index, int length) throws ArrayIndexOutOfBoundsException {
        if (length == 0) {
             return new byte[0];
        }
        
        int end = index + length;
        byte[] result = null;
        
        // All overwritten
        if (overwritten.nextClearBit(index) >= end) {
            result = Arrays.copyOfRange(newValues, index, end);
        }
                
        // Nothing overwritten
        if (result == null) {
            int firstOverwritten = overwritten.nextSetBit(index);
            if (firstOverwritten >= end || firstOverwritten == -1) {
                checkCache();
                result = Arrays.copyOfRange(cached, index, end);
            }
        }
        
        // General case - some overwritten
        if (result == null) {
            result = super.getBytes(index, length);
        }
        
        return result;
    }
    
    private void initNewValues() {
        if (newValues == null) {
            this.newValues = new byte[length()];
        }
    }
    
    @Override
    public void setByte(int index, byte b) throws ArrayIndexOutOfBoundsException {
        vm.checkForIllegalMutation(wrapped);

        initNewValues();
        newValues[index] = b;
        overwritten.set(index);
    }

    @Override
    public void setBytes(int index, byte[] b) throws ArrayIndexOutOfBoundsException {
        vm.checkForIllegalMutation(wrapped);

        initNewValues();
        System.arraycopy(b, 0, newValues, index, b.length);
        overwritten.set(index, index + b.length);
    }
}
