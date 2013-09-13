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

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ClassLoaderHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;

public class HologramVerifier extends BetterVerifier {

    private final HologramVirtualMachine vm;
    
    public HologramVerifier(VirtualMachineHolograph vm, ClassLoaderHolograph loader) {
        super(vm, loader);
        this.vm = new HologramVirtualMachine(vm);
    }

    @Override
    protected ClassMirror getClassMirror(Type t) {
        String className = t.getClassName();
        String tPackage = className.substring(0, className.lastIndexOf('.'));
        if (tPackage.equals("edu.ubc.mirrors") || tPackage.equals("edu.ubc.mirrors.holograms") || className.equals(Throwable.class.getName())) {
            return HologramClassMirror.getFrameworkClassMirror(className);
        }
        Type originalType = HologramClassGenerator.getOriginalType(t);
        boolean isImplementation = HologramClassGenerator.isImplementationClass(className);
        ClassHolograph klass = (ClassHolograph)super.getClassMirror(originalType);
        return new HologramClassMirror(vm, klass, isImplementation);
    }
    
}
