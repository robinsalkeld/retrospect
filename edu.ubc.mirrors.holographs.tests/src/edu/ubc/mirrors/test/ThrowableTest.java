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
package edu.ubc.mirrors.test;

public class ThrowableTest extends Throwable {

    static class STE {
	StackTraceElement wrapped;
	@Override
	public String toString() {
	    return wrapped.toString();
	}
    }

    public ThrowableTest fillInSuperStackTrace() {
	super.fillInStackTrace();
	return this;
    }
    
    public synchronized ThrowableTest fillInStackTrace() {
        return (ThrowableTest)super.fillInStackTrace();
    };
    
    STE getStackTraceElement(int index) {
	return getStackTraceElement(this, index);
    }
    
    int getStackTraceDepth() {
	return getStackTraceDepth(this);
    }
    
    static STE getStackTraceElement(ThrowableTest tt, int index) {
	STE result = new STE();
	result.wrapped = tt.getSuperStackTrace()[index];
	return result;
    }
    
    static int getStackTraceDepth(ThrowableTest tt) {
	return tt.getSuperStackTrace().length;
    }
    
    StackTraceElement[] getSuperStackTrace() {
	return super.getStackTrace();
    }
    
    private STE[] stackTrace = null;
    
    private synchronized STE[] getOurStackTrace() {
        // Initialize stack trace if this is the first call to this method
        if (stackTrace == null) {
            int depth = getStackTraceDepth();
            stackTrace = new STE[depth];
            for (int i=0; i < depth; i++)
                stackTrace[i] = getStackTraceElement(i);
        }
        return stackTrace;
    }
    
    @Override
    public StackTraceElement[] getStackTrace() {
	STE[] clone = getOurStackTrace().clone();
	StackTraceElement[] result = new StackTraceElement[clone.length];
	for (int i = 0; i < result.length; i++) {
	    result[i] = clone[i].wrapped;
	}
	return result;
    }
    
    public static void main(String[] args) throws Throwable {
	Throwable t = foo();
	t.printStackTrace();
	t.fillInStackTrace();
	t.printStackTrace();
    }
    
    private static Throwable foo() throws Throwable {
	return bar();
    }
    
    private static Throwable bar() throws Throwable {
	return new RuntimeException();
    }
}
