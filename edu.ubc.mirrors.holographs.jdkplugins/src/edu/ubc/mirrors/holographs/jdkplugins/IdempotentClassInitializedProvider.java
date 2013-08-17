package edu.ubc.mirrors.holographs.jdkplugins;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassMirrorInitializedProvider;

public class IdempotentClassInitializedProvider implements ClassMirrorInitializedProvider {

    private static final Set<String> idempotentClassInits = new HashSet<String>(Arrays.asList(
            "java.lang.reflect.Modifier",
            "java.net.AbstractPlainSocketImpl",
            "java.net.URLClassLoader",
            "java.security.KeyFactory",
            "java.security.SecureClassLoader",
            "org.eclipse.equinox.weaving.hooks.AbstractWeavingHook",
            "org.eclipse.e4.core.di.internal.extensions.EventObjectSupplier",
            "org.eclipse.ui.internal.misc.Policy",
            "sun.reflect.UnsafeStaticFieldAccessorImpl"
            ));
    
    @Override
    public Boolean isInitialized(ClassMirror classMirror) {
        if (idempotentClassInits.contains(classMirror.getClassName())) {
            return false;
        }

        return null;
    }

}
