package edu.ubc.mirrors.raw.nativestubs.java.security;

import java.security.AccessController;
import java.security.PrivilegedAction;

import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.mirages.ObjectMirage;

public class AccessControllerStubs {

    
    public static ObjectMirror doPrivileged(ObjectMirror action) {
        Object result = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return null;
            }
        });
        return ObjectMirage.getMirror(result);
    }
}
