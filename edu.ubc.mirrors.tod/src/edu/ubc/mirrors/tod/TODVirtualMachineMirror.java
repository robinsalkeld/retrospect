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
package edu.ubc.mirrors.tod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tod.core.database.browser.ICompoundInspector.EntryValue;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class TODVirtualMachineMirror implements VirtualMachineMirror {

    private final ILogBrowser logBrowser;
    
    public TODVirtualMachineMirror(ILogBrowser logBrowser) {
        super();
        this.logBrowser = logBrowser;
    }

    public ClassMirror makeClassMirror(ITypeInfo type) {
        return null;
    }
    
    public ThreadMirror makeThreadMirror(IThreadInfo threadInfo) {
        return null;
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ThreadMirror> getThreads() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClassMirror getPrimitiveClass(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MirrorEventRequestManager eventRequestManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MirrorEventQueue eventQueue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void suspend() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean canBeModified() {
        return false;
    }

    @Override
    public EventDispatch dispatch() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ClassMirror> findAllClasses() {
        // TODO-RS: This should be all classes at a particular timestamp,
        // not all classes ever!
        List<ClassMirror> result = new ArrayList<ClassMirror>();
        for (IClassInfo classInfo : logBrowser.getStructureDatabase().getClasses()) {
            result.add(makeClassMirror(classInfo));
        }
        return result;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InstanceMirror getInternedString(String s) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object wrapEntryValues(EntryValue[] values) {
        if (values.length != 1) {
            throw new IllegalStateException("Missing/ambiguous values: " + Arrays.toString(values));
        }
        return wrapValue(values[0]);
    }
    
    public Object wrapValue(Object value) {
        return value;
    }
}
