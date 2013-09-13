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
package edu.ubc.mirrors.raw.nativestubs.java.security;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.NativeStubs;
import edu.ubc.mirrors.holographs.jdkplugins.StubMethod;

public class AccessControllerStubs extends NativeStubs {

    public AccessControllerStubs(ClassHolograph klass) {
	super(klass);
    }

    private static MethodHandle PA_RUN_HANDLE = new MethodHandle() {
        protected void methodCall() throws Throwable {
            ((PrivilegedAction<?>)null).run();
        }
    };
    private static MethodHandle PEA_RUN_HANDLE = new MethodHandle() {
        protected void methodCall() throws Throwable {
            ((PrivilegedExceptionAction<?>)null).run();
        }
    };
    
    // TODO-RS: Note this one stub actually covers two overloads of this method:
    // one takes a PrivilagedAction and one takes a PrivilagedExceptionAction.
    // The stubs mechanism should have better support for overloading in general...
    @StubMethod
    public ObjectMirror doPrivileged(final InstanceMirror action) throws Throwable {
        try {
            return (ObjectMirror)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    ClassMirror paeClassMirror = getVM().findBootstrapClassMirror(PrivilegedExceptionAction.class.getName());
                    if (Reflection.isAssignableFrom(paeClassMirror, action.getClassMirror())) {
                        return PEA_RUN_HANDLE.invokeWithErrors(action, ThreadHolograph.currentThreadMirror());
                    } else {
                        return PA_RUN_HANDLE.invokeWithErrors(action, ThreadHolograph.currentThreadMirror());
                    }
                }
            });
        } catch (PrivilegedActionException t) {
            Exception runException = t.getException();
            if (runException instanceof MirrorInvocationTargetException) {
                MirrorInvocationTargetException ite = (MirrorInvocationTargetException)runException;
                InstanceMirror causeMirror = ite.getTargetException();
                
                ClassMirror runtimeExceptionClass = klass.getVM().findBootstrapClassMirror(RuntimeException.class.getName());
                ClassMirror errorClass = klass.getVM().findBootstrapClassMirror(Error.class.getName());
                ClassMirror causeClass = causeMirror.getClassMirror();
                if (Reflection.isAssignableFrom(runtimeExceptionClass, causeClass)
                        || Reflection.isAssignableFrom(errorClass, causeClass)) {
                    throw ite;
                } else {
                    ClassMirror paeClass = klass.getVM().findBootstrapClassMirror(PrivilegedActionException.class.getName());
                    ClassMirror exceptionClass = klass.getVM().findBootstrapClassMirror(Exception.class.getName());
                    InstanceMirror toThrowMirror = paeClass.getConstructor(exceptionClass).newInstance(ThreadHolograph.currentThreadMirror(), causeMirror);
                    toThrowMirror.set(paeClass.getDeclaredField("exception"), causeMirror);
                    throw new MirrorInvocationTargetException(toThrowMirror);
                }
            }
            
            // Anything else is an internal error
            throw (InternalError)new InternalError().initCause(runException);
        }
    }
    
    @StubMethod
    public ObjectMirror doPrivileged(final InstanceMirror action, final InstanceMirror context) throws Throwable {
        // TODO-RS: Need to implement the context behaviour, although it's possible holographic execution
        // can safely ignore it...
	return doPrivileged(action);
    }
    
    @StubMethod
    public InstanceMirror getStackAccessControlContext() {
        // TODO-RS: See above.
        return null;
    }
}
