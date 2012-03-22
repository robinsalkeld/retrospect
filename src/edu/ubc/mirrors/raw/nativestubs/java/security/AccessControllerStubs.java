
package edu.ubc.mirrors.raw.nativestubs.java.security;

import java.security.AccessController;
import java.security.PrivilegedAction;

import edu.ubc.mirrors.mirages.Mirage;

public class AccessControllerStubs {

    
    public static Mirage doPrivileged(Class<?> classLoaderLiteral, Mirage action) {
        Object result = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return null;
            }
        });
        return (Mirage)result;
    }
}
