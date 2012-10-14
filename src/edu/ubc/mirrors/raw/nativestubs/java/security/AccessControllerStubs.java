
package edu.ubc.mirrors.raw.nativestubs.java.security;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.ObjectMirage;
import edu.ubc.mirrors.mirages.Reflection;

public class AccessControllerStubs {

    
    public static Mirage doPrivileged(Class<?> classLoaderLiteral, final Mirage action) throws Throwable {
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
        	Throwable toThrow = ObjectMirage.throwableAsMirage(ObjectMirage.getVM(classLoaderLiteral), t);
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
    
    public static Mirage doPrivileged(Class<?> classLoaderLiteral, final Mirage action, final Mirage context) throws Throwable {
        // TODO-RS: Hoping the context doesn't matter for my examples...
	return doPrivileged(classLoaderLiteral, action);
    }
    
    public static Mirage getStackAccessControlContext(Class<?> classLoaderLiteral) {
        // TODO-RS: Not correct in general, but adequate for now
        return null;
    }
}
