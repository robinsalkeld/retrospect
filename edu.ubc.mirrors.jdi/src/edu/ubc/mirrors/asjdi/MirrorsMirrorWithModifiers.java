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
package edu.ubc.mirrors.asjdi;

import java.lang.reflect.Modifier;

import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public abstract class MirrorsMirrorWithModifiers extends MirrorsMirror {

    public MirrorsMirrorWithModifiers(MirrorsVirtualMachine vm, Object wrapped) {
        super(vm, wrapped);
    }

    public abstract int modifiers();

    public boolean isFinal() {
        return Modifier.isFinal(modifiers());
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers());
    }

    public boolean isSynthetic() {
        return (0x00001000 & modifiers()) != 0;
    }

    public boolean isPackagePrivate() {
        return !(isPublic() || isProtected() || isPrivate());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(modifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(modifiers());
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers());
    }

    public boolean isBridge() {
        return (0x00000040 & modifiers()) != 0;
    }

    public boolean isNative() {
        return Modifier.isNative(modifiers());
    }

    public boolean isSynchronized() {
        return Modifier.isSynchronized(modifiers());
    }
    
    public boolean isVarArgs() {
        return (0x00000080 & modifiers()) != 0;
    }

    public boolean isTransient() {
        return Modifier.isTransient(modifiers());
    }
    
    public boolean isVolatile() {
        return Modifier.isVolatile(modifiers());
    }
    
    public boolean isEnum() {
        return (0x00004000 & modifiers()) != 0;
    }
    
}
