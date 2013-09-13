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
package edu.ubc.mirrors.eclipse.mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class HeapDumpClassMirrorLoader extends HeapDumpInstanceMirror implements ClassMirrorLoader {

    private final HeapDumpVirtualMachineMirror vm;
    private final Map<String, IClass> loadedClasses = new HashMap<String, IClass>();
    
    public HeapDumpClassMirrorLoader(HeapDumpVirtualMachineMirror vm, IClassLoader heapDumpClassLoader) {
        super(vm, heapDumpClassLoader);
        this.vm = vm;
        
        try {
            for (IClass klass : heapDumpClassLoader.getDefinedClasses()) {
                loadedClasses.put(klass.getName(), klass);
            }
        } catch (SnapshotException e) {
            throw new InternalError();
        }
    }
    
    public VirtualMachineMirror getVM() {
        return vm;
    }
    
    @Override
    public List<ClassMirror> loadedClassMirrors() {
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (IClass klass : loadedClasses.values()) {
            result.add((ClassMirror)vm.makeMirror(klass));
        }
        return result;
    }
    
    @Override
    public HeapDumpClassMirror findLoadedClassMirror(String name) {
        IClass klass = loadedClasses.get(name);
        if (klass != null) {
            return (HeapDumpClassMirror)vm.makeMirror(klass);
        }

        return null;
    }
    
    @Override
    public ClassMirror defineClass(String name, ByteArrayMirror b, int off,
            int len, InstanceMirror pd, InstanceMirror source, boolean unsafe) {
        throw new UnsupportedOperationException();
    }
}
