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

import java.io.File;
import java.util.List;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror;

public class DefinedClassMirror extends BytecodeClassMirror implements NewInstanceMirror {
    
    private final boolean unsafe;
    
    public DefinedClassMirror(VirtualMachineHolograph vm, ClassLoaderHolograph loader, String className, byte[] bytecode, boolean unsafe) {
        super(className);
        this.vm = vm;
        this.loader = loader;
        this.bytecode = bytecode;
        this.unsafe = unsafe;
    }

    private final VirtualMachineHolograph vm;
    private final ClassLoaderHolograph loader;
    private final byte[] bytecode;
    
    @Override
    public VirtualMachineMirror getVM() {
        return vm.getWrappedVM();
    }
    
    @Override
    public ClassMirrorLoader getLoader() {
        return loader == null ? null : (ClassMirrorLoader)loader.getWrapped();
    }
    
    @Override
    protected ClassMirror loadClassMirrorInternal(Type type) {
        // Need to use ThreadHolograph.currentThreadMirrorNoError() in case we are trying to load a bootstrap
        // class and don't have an active thread. This is safe since downstream methods will check for a valid
        // thread if they need one.
        ClassMirror classHolograph = HolographInternalUtils.classMirrorForType(vm, ThreadHolograph.currentThreadMirrorNoError(), type, false, loader);
        return unwrapClassMirror(classHolograph);
    }
    
    private ClassMirror unwrapClassMirror(ClassMirror klass) {
        if (klass instanceof ClassHolograph) {
            return ((ClassHolograph)klass).getWrappedClassMirror();
        } else if (klass.isArray()) {
            return new ArrayClassMirror(1, unwrapClassMirror(klass.getComponentClassMirror()));
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public byte[] getBytecode() {
        return bytecode;
    }
    
    @Override
    public boolean initialized() {
        return false;
    }
    
    @Override
    public int identityHashCode() {
        return hashCode();
    }
    
    @Override
    public void bytecodeLocated(File originalBytecodeLocation) {
    }

    public boolean isUnsafe() {
        return unsafe;
    }
    
    @Override
    public List<ClassMirror> getSubclassMirrors() {
        // TODO-RS: Need more tracking to implement this, but doable.
        throw new UnsupportedOperationException();
    }
    
    @Override
    public FieldMirror createField(int modifiers, ClassMirror type, String name) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean canLock() {
        return true;
    }
}
