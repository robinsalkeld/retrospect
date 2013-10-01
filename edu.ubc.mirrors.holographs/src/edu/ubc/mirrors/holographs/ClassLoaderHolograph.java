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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class ClassLoaderHolograph extends InstanceHolograph implements ClassMirrorLoader {

    protected final ClassMirrorLoader wrappedLoader;
    
    private final Map<String, ClassHolograph> dynamicallyDefinedClasses =
            new HashMap<String, ClassHolograph>();
      
    protected ClassLoaderHolograph(VirtualMachineHolograph vm, ClassMirrorLoader wrappedLoader) {
        super(vm, wrappedLoader);
        this.wrappedLoader = wrappedLoader;
        this.hologramLoader = new HologramClassLoader(vm, this);
    }

    private final HologramClassLoader hologramLoader;
    
    public HologramClassLoader getHologramClassLoader() {
        return hologramLoader;
    }
    
    @Override
    public List<ClassMirror> loadedClassMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (ClassMirror klass : wrappedLoader.loadedClassMirrors()) {
            result.add(vm.getWrappedClassMirror(klass));
        }
        result.addAll(dynamicallyDefinedClasses.values());
        return result;
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(String name) {
        ClassMirror result = wrappedLoader.findLoadedClassMirror(name);
        if (result != null) {
            return vm.getWrappedClassMirror(result);
        }
        
        return dynamicallyDefinedClasses.get(name);
    }
    
    @Override
    public ClassMirror defineClass(String name, ByteArrayMirror b, int off, int len,
            InstanceMirror pd, InstanceMirror source, boolean unsafe) {
        
        if (findLoadedClassMirror(name) != null) {
            throw new IllegalArgumentException("Attempt to define already defined class: " + name);
        }
        
        final byte[] realBytecode = new byte[len];
        Reflection.arraycopy(b, off, new NativeByteArrayMirror(realBytecode), 0, len);
        
        ClassMirror newClass = new DefinedClassMirror(vm, this, name, realBytecode, unsafe);
        final ClassHolograph newClassHolograph = (ClassHolograph)vm.getWrappedClassMirror(newClass);
        dynamicallyDefinedClasses.put(name, newClassHolograph);
        
        newClassHolograph.registerPrepareCallback();
        
        return newClassHolograph;
    }
}
