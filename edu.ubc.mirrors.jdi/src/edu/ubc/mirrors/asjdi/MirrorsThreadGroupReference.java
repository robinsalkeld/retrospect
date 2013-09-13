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

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.asjdi.MirrorsInstanceReference;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsThreadGroupReference extends MirrorsInstanceReference implements ThreadGroupReference {

    private final ClassMirror threadGroupClass;
    
    public MirrorsThreadGroupReference(MirrorsVirtualMachine vm, InstanceMirror wrapped) {
        super(vm, wrapped);
        this.threadGroupClass = vm.vm.findBootstrapClassMirror(ThreadGroup.class.getName());
    }

    @Override
    public String name() {
        return Reflection.getRealStringForMirror(readField(threadGroupClass, "name"));
    }

    @Override
    public ThreadGroupReference parent() {
        return (ThreadGroupReference)vm.wrapMirror(readField(threadGroupClass, "parent"));
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
    public List<ThreadGroupReference> threadGroups() {
        List<ThreadGroupReference> result = new ArrayList<ThreadGroupReference>();
        for (ObjectMirror group : Reflection.fromArray((ObjectArrayMirror)readField(threadGroupClass, "groups"))) {
            result.add((ThreadGroupReference)vm.wrapMirror(group));
        }
        return result;
    }

    @Override
    public List<ThreadReference> threads() {
        List<ThreadReference> result = new ArrayList<ThreadReference>();
        for (ObjectMirror group : Reflection.fromArray((ObjectArrayMirror)readField(threadGroupClass, "threads"))) {
            result.add((ThreadReference)vm.wrapMirror(group));
        }
        return result;
    }
}
