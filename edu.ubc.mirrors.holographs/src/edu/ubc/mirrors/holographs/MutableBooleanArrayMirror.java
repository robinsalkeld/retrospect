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

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.wrapping.WrappingBooleanArrayMirror;

public class MutableBooleanArrayMirror extends WrappingBooleanArrayMirror {

    private final VirtualMachineHolograph vm;
    private final BooleanArrayMirror immutableMirror;
    private boolean[] values;
    
    public MutableBooleanArrayMirror(VirtualMachineHolograph vm, BooleanArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.vm = vm;
        this.immutableMirror = immutableMirror;
        
    }
    
    @Override
    public boolean getBoolean(int index) throws ArrayIndexOutOfBoundsException {
        return values != null ? values[index] : immutableMirror.getBoolean(index);
    }

    @Override
    public void setBoolean(int index, boolean b) throws ArrayIndexOutOfBoundsException {
        vm.checkForIllegalMutation(wrapped);
        
        if (values == null) {
            this.values = new boolean[immutableMirror.length()];
            for (int i = 0; i < values.length; i++) {
                values[i] = immutableMirror.getBoolean(i);
            }
        }
        values[index] = b;
    }
}
