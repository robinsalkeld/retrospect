
package edu.ubc.mirrors.raw.nativestubs.java.security;

import java.security.AccessController;
import java.security.PrivilegedAction;

import edu.ubc.mirrors.mirages.Mirage;
import edu.ubc.mirrors.mirages.Reflection;

public class AccessControllerStubs {

    
    public static Mirage doPrivileged(Class<?> classLoaderLiteral, final Mirage action) {
        return (Mirage)AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return Reflection.invoke(action, "run");
            }
        });
    }
    
    public static Mirage doPrivileged(Class<?> classLoaderLiteral, final Mirage action, final Mirage context) {
        // TODO-RS: Hoping the context doesn't matter for my examples...
        return (Mirage)AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return Reflection.invoke(action, "run");
            }
        });
    }
    
    public static Mirage getStackAccessControlContext(Class<?> classLoaderLiteral) {
        // TODO-RS: Not correct in general, but adequate for now
        return null;
    }
}
