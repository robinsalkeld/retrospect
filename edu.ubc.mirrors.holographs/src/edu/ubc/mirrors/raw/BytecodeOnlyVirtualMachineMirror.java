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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class BytecodeOnlyVirtualMachineMirror implements VirtualMachineMirror {

    private final VirtualMachineMirror baseVM;
    
    // May be null
    private final ClassLoader bootstrapLoader;
    
    private final Map<String, ClassMirror> bootstrapClasses = new HashMap<String, ClassMirror>();
    
    public BytecodeOnlyVirtualMachineMirror(VirtualMachineMirror baseVM, ClassLoader bootstrapLoader) {
        this.baseVM = baseVM;
        this.bootstrapLoader = bootstrapLoader;
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        ClassMirror result = bootstrapClasses.get(name);
        if (result != null) {
            return result;
        }
        
        // Note using null as the ClassMirrorLoader since the bootstrapLoader acts like the
        // VM's bootstrap loader, which is sometimes (and in this case) represented by null.
        result = BytecodeClassMirrorLoader.loadBytecodeClassMirror(this, null, bootstrapLoader, Type.getObjectType(name.replace('.', '/')));
        bootstrapClasses.put(name, result);
        return result;
    }
    
    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        throw new UnsupportedOperationException(); 
    }
    
    @Override
    public List<ClassMirror> findAllClasses() {
        throw new UnsupportedOperationException(); 
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        throw new UnsupportedOperationException(); 
    }    
    
    @Override
    public List<ThreadMirror> getThreads() {
        throw new UnsupportedOperationException(); 
    }
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        return baseVM.getPrimitiveClass(name);
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        return baseVM.getArrayClass(dimensions, elementClass);
    }

    @Override
    public MirrorEventRequestManager eventRequestManager() {
	throw new UnsupportedOperationException(); 
    }
    
    @Override
    public MirrorEventQueue eventQueue() {
	throw new UnsupportedOperationException(); 
    }

    @Override
    public void resume() {
	throw new UnsupportedOperationException(); 
    }
    
    @Override
    public void suspend() {
	throw new UnsupportedOperationException(); 
    }
    
    @Override
    public boolean canBeModified() {
        return false;
    }
    
    @Override
    public boolean canGetBytecodes() {
        return true;
    }
    
    @Override
    public boolean hasClassInitialization() {
        return false;
    }
    
    @Override
    public InstanceMirror makeString(String s) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public InstanceMirror getInternedString(String s) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public EventDispatch dispatch() {
        throw new UnsupportedOperationException();
    }
}
