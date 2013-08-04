
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
                ClassMirror paeClass = klass.getVM().findBootstrapClassMirror(PrivilegedActionException.class.getName());
                ClassMirror exceptionClass = klass.getVM().findBootstrapClassMirror(Exception.class.getName());
                InstanceMirror toThrowMirror = paeClass.getConstructor(exceptionClass).newInstance(ThreadHolograph.currentThreadMirror(), causeMirror);
                toThrowMirror.set(paeClass.getDeclaredField("exception"), causeMirror);
                throw new MirrorInvocationTargetException(toThrowMirror);
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
