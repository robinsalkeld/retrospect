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

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.DefinedClassMirror;
import edu.ubc.mirrors.holograms.HologramClassGenerator;
import edu.ubc.mirrors.holograms.HologramClassMirror;
import edu.ubc.mirrors.holograms.HologramVirtualMachine;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.wrapping.WrappingClassMirror;

public class HologramClassMirror extends WrappingClassMirror {

    private final HologramVirtualMachine vm;
    private final boolean isImplementationClass;
    
    protected HologramClassMirror(HologramVirtualMachine vm, ClassMirror wrapped, boolean isImplementationClass) {
        super(vm, wrapped);
        this.vm = vm;
        this.isImplementationClass = isImplementationClass;
    }
    
    public ClassMirror getOriginal() {
        return wrapped;
    }
    
    public boolean isImplementationClass() {
        return isImplementationClass;
    }
    
    @Override
    public boolean isInterface() {
        if (getOriginal().getClassName().equals(Object.class.getName()) && !isImplementationClass) {
            return true;
        } else {
            return super.isInterface();
        }
    }
    
    @Override
    public String getClassName() {
        return HologramClassGenerator.getHologramBinaryClassName(getOriginal().getClassName(), isImplementationClass);
    }
    
    @Override
    public byte[] getBytecode() {
        return ClassHolograph.getHologramClassLoader(getOriginal()).getBytecode(this);
    }
    
    public static ClassMirror getFrameworkClassMirror(String className) {
        try {
            return new NativeClassMirror(Class.forName(className, false, HologramClassMirror.class.getClassLoader()));
        } catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }
    
    @Override
    public ClassMirror getSuperClassMirror() {
        if (getOriginal().getClassName().equals(Throwable.class.getName())) {
            return new NativeClassMirror(Throwable.class);
        }
        ClassMirror original = wrapped.getSuperClassMirror();
        if (original == null) {
            return null;
        }
        
        
        HologramClassMirror hologramMirror = new HologramClassMirror(vm, original, true);
        if (hologramMirror.getClassName().startsWith("edu.ubc.mirrors")) {
            return getFrameworkClassMirror(hologramMirror.getClassName());
        }
        return hologramMirror;
    }
    
    public boolean isUnsafe() {
        if (wrapped instanceof ClassHolograph) {
            ClassMirror inner = ((ClassHolograph)wrapped).getWrappedClassMirror();
            if (inner instanceof DefinedClassMirror) {
                return ((DefinedClassMirror)inner).isUnsafe();
            }
        }
        return false;
    }
    
}
