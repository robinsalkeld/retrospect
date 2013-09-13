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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;

public class FrameValue implements Value {

    private final BasicValue value;
    
    /**
     */
    private final AbstractInsnNode newInsn;
    
    private static final AbstractInsnNode UNINITIALIZED_THIS_INSN = new TypeInsnNode(Opcodes.NEW, null);
    private static final AbstractInsnNode INITIALIZED = null;
    
    public FrameValue(BasicValue value, AbstractInsnNode newInsn) {
        this.value = value;
        this.newInsn = newInsn;
    }
    
    public BasicValue getBasicValue() {
        return value;
    }
    
    public AbstractInsnNode getNewInsn() {
        return newInsn;
    }
    
    public static FrameValue fromBasicValue(BasicValue value) {
        return value == null ? null : new FrameValue(value, INITIALIZED);
    }
    
    public static FrameValue makeUninitializedThis(BasicValue value) {
        return new FrameValue(value, UNINITIALIZED_THIS_INSN);
    }
    
    public boolean isUninitialized() {
        return newInsn != INITIALIZED;
    }
    
    public boolean isUninitializedThis() {
        return newInsn == UNINITIALIZED_THIS_INSN;
    }
    
    
    @Override
    public int getSize() {
        return value.getSize();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FrameValue)) {
            return false;
        }
        
        FrameValue other = (FrameValue)obj;
        return value.equals(other.value) && newInsn == other.newInsn; 
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public String toString() {
        if (isUninitializedThis()) {
            return "(uninitialized this)";
        } else if (isUninitialized()) {
            return "(uninitialized) " + value;
        } else {
            return value.toString();
        }
    }
}
