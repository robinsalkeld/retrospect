
package edu.ubc.mirrors.raw.nativestubs.java.security;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class AccessControllerStubs extends NativeStubs {

    public AccessControllerStubs(ClassHolograph klass) {
	super(klass);
    }

    public ObjectMirror doPrivileged(final InstanceMirror action) throws Throwable {
        try {
            return (ObjectMirror)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    return action.getClass().getMethod("run").invoke(action);
                }
            });
        } catch (PrivilegedActionException t) {
            Exception runException = t.getException();
            if (runException instanceof InvocationTargetException) {
        	InvocationTargetException ite = (InvocationTargetException)runException;
        	Throwable toThrow = ObjectMirage.throwableAsMirage(getVM(), t);
        	InstanceMirror toThrowMirror = (InstanceMirror)((Mirage)toThrow).getMirror();
        	Throwable cause = ite.getCause();
        	if (cause instanceof Mirage) {
        	    ObjectMirror causeMirror = ((Mirage)cause).getMirror();
        	    ClassMirror paeClass = klass.getVM().findBootstrapClassMirror(PrivilegedActionException.class.getName());
        	    toThrowMirror.set(paeClass.getDeclaredField("exception"), causeMirror);
        	    throw toThrow;
        	}
            }
            
            // Anything else is an internal error
            throw (InternalError)new InternalError().initCause(runException);
        }
    }
    
    public ObjectMirror doPrivileged(final InstanceMirror action, final InstanceMirror context) throws Throwable {
        // TODO-RS: Need to implement the context behaviour, although it's possible holographic execution
        // can safely ignore it...
	return doPrivileged(action);
    }
    
    public Mirage getStackAccessControlContext() {
        // TODO-RS: See above.
        return null;
    }
}
