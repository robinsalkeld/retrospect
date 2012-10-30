
package edu.ubc.mirrors.raw.nativestubs.java.security;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.NativeStubs;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class AccessControllerStubs extends NativeStubs {

    public AccessControllerStubs(ClassHolograph klass) {
	super(klass);
    }

    public Mirage doPrivileged(final Mirage action) throws Throwable {
        try {
            return (Mirage)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
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
        	    toThrowMirror.getMemberField("exception").set(causeMirror);
        	    throw toThrow;
        	}
            }
            
            // Anything else is an internal error
            throw (InternalError)new InternalError().initCause(runException);
        }
    }
    
    public Mirage doPrivileged(final Mirage action, final Mirage context) throws Throwable {
        // TODO-RS: Hoping the context doesn't matter for my examples...
	return doPrivileged(action);
    }
    
    public Mirage getStackAccessControlContext() {
        // TODO-RS: Not correct in general, but adequate for now
        return null;
    }
}
