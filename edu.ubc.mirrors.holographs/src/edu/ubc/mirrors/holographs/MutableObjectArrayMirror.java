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

import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.wrapping.WrappingObjectArrayMirror;

public class MutableObjectArrayMirror extends WrappingObjectArrayMirror {

    private ObjectMirror[] mutableValues;
    private final ObjectArrayMirror immutableMirror;
    
    public MutableObjectArrayMirror(VirtualMachineHolograph vm, ObjectArrayMirror immutableMirror) {
        super(vm, immutableMirror);
        this.immutableMirror = immutableMirror;
        
    }
    
    @Override
    public ObjectMirror get(int index) throws ArrayIndexOutOfBoundsException {
        return mutableValues != null ? mutableValues[index] : vm.getWrappedMirror(immutableMirror.get(index));
    }

    @Override
    public void set(int i, ObjectMirror o) throws ArrayIndexOutOfBoundsException {
        if (mutableValues == null) {
            this.mutableValues = new ObjectMirror[immutableMirror.length()];
            for (int index = 0; index < mutableValues.length; index++) {
                mutableValues[index] = vm.getWrappedMirror(immutableMirror.get(index));
            }
        }
        mutableValues[i] = o;
    }
    
    @Override
    public String toString() {
        return "MutableObjectArrayMirror on " + immutableMirror;
    }

}
