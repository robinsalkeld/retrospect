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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;

public class HeapDumpFrameMirror implements FrameMirror {

    private static final Pattern FRAME_PATTERN = Pattern.compile("at (.*)\\.(.*)\\(.*\\).* \\((?:(.*):(\\d*)|(.*))\\)");
    
    private final ClassMirror declaringClass;
    private final String methodName;
    private final String fileName;
    private final int lineNumber;
    
    public HeapDumpFrameMirror(HeapDumpVirtualMachineMirror vm, String text) {
	Matcher m = FRAME_PATTERN.matcher(text);
        if (m.matches()) {
            String className = m.group(1);
            
            // Unfortunately here we only have class names rather than actual classes
            // so if any are ambiguous we're hooped.
            List<ClassMirror> matchingClasses = vm.findAllClasses(className, false);
            if (matchingClasses.isEmpty()) {
                // This indicates an error in the underlying VM
                throw new InternalError();
            } else if (matchingClasses.size() > 1) {
                // This is just unfortunate but could happen
                throw new InternalError("Ambiguous class name on stack: " + className);
            }
            declaringClass = matchingClasses.get(0);
            methodName = m.group(2);
            fileName = m.group(3);
            String lineTxt = m.group(4);
            lineNumber = (lineTxt != null ? Integer.parseInt(lineTxt) : -1);
        } else {
            throw new RuntimeException("Unexpected frame text format: " + text);
        }

    }
    
    @Override
    public ClassMirror declaringClass() {
        return declaringClass;
    }
    
    @Override
    public String methodName() {
        return methodName;
    }
    
    @Override
    public MethodMirror method() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ConstructorMirror constructor() {
        throw new UnsupportedOperationException();
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
