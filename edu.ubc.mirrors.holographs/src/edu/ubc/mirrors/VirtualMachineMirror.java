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
package edu.ubc.mirrors;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

public interface VirtualMachineMirror {

    public EventDispatch dispatch();
    public void addCallback(MirrorEventRequest request, Callback<MirrorEvent> callback);
    
    public ClassMirror findBootstrapClassMirror(String name);
    
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len);
    
    public Enumeration<URL> findBootstrapResources(String path) throws IOException;
    
    public List<ClassMirror> findAllClasses();
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses); 
    
    public List<ThreadMirror> getThreads();
    
    public ClassMirror getPrimitiveClass(String name);
    
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass);
    
    public MirrorEventRequestManager eventRequestManager();
    
    public MirrorEventQueue eventQueue();
    
    public void suspend();
    public void resume();
    
    public boolean canBeModified();
    // This is also interpreted to mean "can get fields/methods/etc"
    public boolean canGetBytecodes();
    public boolean hasClassInitialization();

    public InstanceMirror makeString(String s);
    public InstanceMirror getInternedString(String s);

}
