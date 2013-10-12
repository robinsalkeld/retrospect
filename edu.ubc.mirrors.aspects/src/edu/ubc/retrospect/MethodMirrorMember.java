package edu.ubc.retrospect;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.UnresolvedType;

import edu.ubc.mirrors.MethodMirror;

public class MethodMirrorMember extends ResolvedMemberImpl {

    private final MethodMirror method;
    
    private MethodMirrorMember(MethodMirror method, UnresolvedType declaringType, int modifiers, UnresolvedType returnType, 
            String name, UnresolvedType[] parameterTypes, UnresolvedType[] checkedExceptions) {
        super(Member.METHOD, declaringType, modifiers, returnType, name, parameterTypes, checkedExceptions);
        this.method = method;
    }

    public MethodMirror getMethod() {
        return method;
    }
    
    public static MethodMirrorMember make(MirrorWorld world, MethodMirror method) {
        UnresolvedType declaringType = world.resolve(method.getDeclaringClass());
        UnresolvedType returnType = world.resolve(method.getReturnType());
        
        UnresolvedType[] parameterTypes = new UnresolvedType[method.getParameterTypes().size()];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = world.resolve(method.getParameterTypes().get(i));
        }
        
        UnresolvedType[] exceptionTypes = new UnresolvedType[method.getExceptionTypes().size()];
        for (int i = 0; i < exceptionTypes.length; i++) {
            exceptionTypes[i] = world.resolve(method.getExceptionTypes().get(i));
        }
        
        return new MethodMirrorMember(method, declaringType, method.getModifiers(), returnType, method.getName(), parameterTypes, exceptionTypes);
    }
}
