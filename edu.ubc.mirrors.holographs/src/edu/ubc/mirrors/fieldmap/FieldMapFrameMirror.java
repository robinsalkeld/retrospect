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
package edu.ubc.mirrors.fieldmap;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;

public class FieldMapFrameMirror implements FrameMirror {

    public FieldMapFrameMirror(MethodMirror method, String fileName, int lineNumber) {
        this.method = method;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    private final MethodMirror method;
    private final String fileName;
    private final int lineNumber;
    
    @Override
    public ClassMirror declaringClass() {
        return method.getDeclaringClass();
    }

    @Override
    public String methodName() {
        return method.getName();
    }

    @Override
    public MethodMirror method() {
        return method;
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public int lineNumber() {
        return lineNumber;
    }

    @Override
    public InstanceMirror thisObject() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<Object> arguments() {
        throw new UnsupportedOperationException();
    }
}
