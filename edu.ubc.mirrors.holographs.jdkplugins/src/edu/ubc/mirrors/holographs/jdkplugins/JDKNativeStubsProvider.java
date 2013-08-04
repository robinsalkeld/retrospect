package edu.ubc.mirrors.holographs.jdkplugins;

import java.util.HashMap;
import java.util.Map;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.holographs.MirrorInvocationHandler;
import edu.ubc.mirrors.holographs.MirrorInvocationHandlerProvider;

public class JDKNativeStubsProvider implements MirrorInvocationHandlerProvider {

    private final Map<ClassMirror, MirrorInvocationHandlerProvider> providersByClass 
        = new HashMap<ClassMirror, MirrorInvocationHandlerProvider>();
    
    private MirrorInvocationHandlerProvider getProvider(ClassMirror classMirror) {
        MirrorInvocationHandlerProvider result = providersByClass.get(classMirror);
        if (result != null) {
            return result;
        }
        
        String nativeStubsName = "edu.ubc.mirrors.raw.nativestubs." + classMirror.getClassName() + "Stubs";
        try {
            Class<?> stubsClass = Class.forName(nativeStubsName);
            result = new NativeStubsProvider(classMirror, stubsClass);
            providersByClass.put(classMirror, result);
            return result;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    @Override
    public MirrorInvocationHandler getInvocationHandler(MethodMirror method) {
        MirrorInvocationHandlerProvider provider = getProvider(method.getDeclaringClass());
        return provider != null ? provider.getInvocationHandler(method) : null;
    }
}
