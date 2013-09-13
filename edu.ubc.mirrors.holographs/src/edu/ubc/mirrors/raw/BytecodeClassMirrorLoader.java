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
package edu.ubc.mirrors.raw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class BytecodeClassMirrorLoader extends NativeInstanceMirror implements ClassMirrorLoader {

    private final VirtualMachineMirror vm;
    private final ClassLoader loader;
    
    private final Map<String, ClassMirror> classes = new HashMap<String, ClassMirror>();
    
    public BytecodeClassMirrorLoader(VirtualMachineMirror vm, ClassLoader loader) {
        super(loader);
        this.vm = vm;
        this.loader = loader;
    }
    
    public static ClassMirror loadBytecodeClassMirror(final VirtualMachineMirror vm, final ClassMirrorLoader loaderMirror, final ClassLoader loader, Type type) {
        if (type.getSort() == Type.OBJECT) {
            String name = type.getClassName();
            final byte[] bytecode = NativeClassMirror.getNativeBytecode(loader, name);
            if (bytecode != null) {
                return new BytecodeClassMirror(name) {
                    @Override
                    public byte[] getBytecode() {
                        return bytecode;
                    }
                    @Override
                    public VirtualMachineMirror getVM() {
                        return vm;
                    }
                    @Override
                    public ClassMirrorLoader getLoader() {
                        return loaderMirror;
                    }
                    @Override
                    protected ClassMirror loadClassMirrorInternal(Type type) {
                        return loadBytecodeClassMirror(vm, loaderMirror, loader, type);
                    }
                    @Override
                    public boolean initialized() {
                        return false;
                    }
                };
            }
            return null;
        } else if (type.getSort() == Type.ARRAY) {
            Type elementType = type.getElementType();
            ClassMirror elementClassMirror;
            elementClassMirror = loadBytecodeClassMirror(vm, loaderMirror, loader, elementType);
            return vm.getArrayClass(type.getDimensions(), elementClassMirror);
        } else {
            return vm.getPrimitiveClass(type.getClassName());
        }
    }
    
    @Override
    public List<ClassMirror> loadedClassMirrors() {
        return new ArrayList<ClassMirror>(classes.values());
    }
    
    @Override
    public ClassMirror findLoadedClassMirror(final String name) {
        ClassMirror result = classes.get(name);
        if (result != null) {
            return result;
        }

        result = loadBytecodeClassMirror(vm, this, loader, Type.getObjectType(name.replace('.', '/')));
        classes.put(name, result);
        return result;
    }
    
    @Override
    public ClassMirror defineClass(String name, ByteArrayMirror b, int off,
            int len, InstanceMirror pd, InstanceMirror source, boolean unsafe) {
        throw new UnsupportedOperationException();
    }
}
