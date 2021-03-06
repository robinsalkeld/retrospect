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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.asjdi.MirrorsLocation;
import edu.ubc.mirrors.asjdi.MirrorsMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;

public class MirrorsStackFrame extends MirrorsMirror implements StackFrame {

    private final FrameMirror frame;
    private final ThreadReference thread;
    
    public MirrorsStackFrame(MirrorsVirtualMachine vm, ThreadReference thread, FrameMirror frame) {
        super(vm, frame);
        this.frame = frame;
        this.thread = thread;
    }

    @Override
    public List<Value> getArgumentValues() {
        return Collections.emptyList();
    }

    @Override
    public Value getValue(LocalVariable arg0) {
        throw new IllegalArgumentException();
    }

    @Override
    public Map<LocalVariable, Value> getValues(List<? extends LocalVariable> vars) {
        if (vars.isEmpty()) {
            return Collections.emptyMap();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Location location() {
        return new MirrorsLocation(vm, frame.method(), frame.fileName(), frame.lineNumber());
    }

    @Override
    public void setValue(LocalVariable arg0, Value arg1)
            throws InvalidTypeException, ClassNotLoadedException {
        throw new IllegalArgumentException();
    }

    @Override
    public ObjectReference thisObject() {
        // TODO-RS: Not available at all in heap dump model, or at least not the Eclipse MAT version.
        return null;
    }

    @Override
    public ThreadReference thread() {
        return thread;
    }

    @Override
    public LocalVariable visibleVariableByName(String arg0) throws AbsentInformationException {
        throw new AbsentInformationException();
    }

    @Override
    public List<LocalVariable> visibleVariables() throws AbsentInformationException {
        throw new AbsentInformationException();
    }

}
