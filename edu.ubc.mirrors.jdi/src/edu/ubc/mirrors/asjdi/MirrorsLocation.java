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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.MethodMirror;

public class MirrorsLocation implements Location {

    private final MirrorsVirtualMachine vm;
    public MirrorsLocation(MirrorsVirtualMachine vm,
            MethodMirror method, String fileName,
            int lineNumber) {
        this.vm = vm;
        this.method = method;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    private final MethodMirror method;
    private final String fileName;
    private int lineNumber;
    
    @Override
    public VirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public int compareTo(Locatable o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public long codeIndex() {
        return -1;
    }
    
    @Override
    public ReferenceType declaringType() {
        return (ReferenceType)vm.typeForClassMirror(method.getDeclaringClass());
    }
    
    @Override
    public int lineNumber() {
        return lineNumber;
    }
    
    @Override
    public int lineNumber(String arg0) {
        return lineNumber;
    }
    
    @Override
    public Method method() {
        return new MethodMirrorMethod(vm, method);
    }
    
    @Override
    public String sourceName() throws AbsentInformationException {
        return fileName;
    }
    
    @Override
    public String sourceName(String arg0) throws AbsentInformationException {
        return fileName;
    }
    
    @Override
    public String sourcePath() throws AbsentInformationException {
        throw new AbsentInformationException();
    }
    
    @Override
    public String sourcePath(String arg0) throws AbsentInformationException {
        throw new AbsentInformationException();
    }
    
    public static Location locationForMethod(final MirrorsVirtualMachine vm, final MethodMirror method) {
        return new MirrorsLocation(vm, method, method.getDeclaringClass().getClassName().replace('.', '/') + ".java", -1) {
            @Override
            public Method method() {
                return new MethodMirrorMethod(vm, method);
            }
        };
    }
}
