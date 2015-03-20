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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.MirrorInvocationHandlerProvider;

public class NativeStubsProvider implements MirrorInvocationHandlerProvider {

    public NativeStubsProvider(ClassMirror classMirror, Class<?> stubsClass) {
        try {
            this.stubsClassInstance = (NativeStubs)stubsClass.getConstructor(ClassHolograph.class).newInstance(classMirror);
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        this.handlers = indexStubMethods(stubsClass);
    }

    private final NativeStubs stubsClassInstance;
    private final Map<org.objectweb.asm.commons.Method, MirrorInvocationHandler> handlers;
    
    private Map<org.objectweb.asm.commons.Method, MirrorInvocationHandler> indexStubMethods(Class<?> nativeStubsClass) {
        Map<org.objectweb.asm.commons.Method, MirrorInvocationHandler> result = new HashMap<org.objectweb.asm.commons.Method, MirrorInvocationHandler>();
        for (Method stubsMethod : nativeStubsClass.getMethods()) {
            if (stubsMethod.getAnnotation(StubMethod.class) != null) {
                org.objectweb.asm.commons.Method method = org.objectweb.asm.commons.Method.getMethod(stubsMethod);
                MirrorInvocationHandler handler = new NativeStubsInvocationHandler(stubsClassInstance, stubsMethod);
                result.put(method, handler);
            }
        }
        return result;
    }
    
    public static org.objectweb.asm.commons.Method getStubMethodDesc(MethodMirror method) {
        Type methodType = Reflection.getMethodType(method);
        Type[] argumentTypes = methodType.getArgumentTypes();
        List<Type> stubArgumentTypes = new ArrayList<Type>(argumentTypes.length + 1);
        if ((Opcodes.ACC_STATIC & method.getModifiers()) == 0) {
            stubArgumentTypes.add(Reflection.getMirrorType(Reflection.typeForClassMirror(method.getDeclaringClass())));
        }
        for (int i = 0; i < argumentTypes.length; i++) {
            stubArgumentTypes.add(Reflection.getMirrorType(argumentTypes[i]));
        }
        Type stubReturnType = Reflection.getMirrorType(methodType.getReturnType());
        return new org.objectweb.asm.commons.Method(method.getName(), stubReturnType, stubArgumentTypes.toArray(new Type[stubArgumentTypes.size()]));
    }
    
    
    
    @Override
    public MirrorInvocationHandler getInvocationHandler(MethodMirror method) {
        return handlers.get(getStubMethodDesc(method));
    }

}
