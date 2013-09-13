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

import java.util.List;

public interface MethodMirror {
    
    public ClassMirror getDeclaringClass();
    
    public int getSlot();
    
    public int getModifiers();
    
    public String getName();
    public List<String> getParameterTypeNames();
    public List<ClassMirror> getParameterTypes();
    public List<String> getExceptionTypeNames();
    public List<ClassMirror> getExceptionTypes();
    public String getReturnTypeName();
    public ClassMirror getReturnType();
    public String getSignature();
    
    // TODO-RS: Should we have a different InvocationTargetException that has a mirror as a cause instead?
    public Object invoke(ThreadMirror thread, ObjectMirror obj, Object ... args) throws IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException;
    
    public void setAccessible(boolean flag);
    
    public byte[] getRawAnnotations();
    public byte[] getRawParameterAnnotations();
    public byte[] getRawAnnotationDefault();
}
