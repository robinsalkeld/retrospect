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
package edu.ubc.mirrors.holographs.jdkplugins;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.ClassMirrorInitializedProvider;

public class IdempotentClassInitializedProvider implements ClassMirrorInitializedProvider {

    private static final Set<String> idempotentClassInits = new HashSet<String>(Arrays.asList(
            "java.lang.reflect.Modifier",
//            "java.net.URLClassLoader",
            "java.security.KeyFactory"
//            "java.security.SecureClassLoader",
//            "org.eclipse.equinox.weaving.hooks.AbstractWeavingHook",
//            "org.eclipse.e4.core.di.internal.extensions.EventObjectSupplier",
//            "org.eclipse.ui.internal.misc.Policy",
//            "sun.reflect.UnsafeStaticFieldAccessorImpl"
            ));
    
    @Override
    public Boolean isInitialized(ClassMirror classMirror) {
        if (idempotentClassInits.contains(classMirror.getClassName())) {
            return false;
        }

        return null;
    }

}
