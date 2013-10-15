package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.Pointcut;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.Reflection;

public class MethodMirrorMember extends ResolvedMemberImpl {

    private final MirrorWorld world;
    protected final MethodMirror method;
    
    private MethodMirrorMember(MirrorWorld world, MethodMirror method, UnresolvedType declaringType, int modifiers, UnresolvedType returnType, 
            String name, UnresolvedType[] parameterTypes, UnresolvedType[] checkedExceptions) {
        super(Member.METHOD, declaringType, modifiers, returnType, name, parameterTypes, checkedExceptions);
        this.world = world;
        this.method = method;
    }

    public MethodMirror getMethod() {
        return method;
    }
    
    @Override
    public ShadowMunger getAssociatedShadowMunger() {
        for (AdviceKind kind : MirrorWorld.SUPPORTED_ADVICE_KINDS) {
            AnnotationMirror annot = Reflection.getAnnotation(method.getAnnotations(), world.getAnnotClassMirror(kind));
            if (annot != null) {
                String pointcut = (String)annot.getValue("value");
                Pointcut pc = world.parsePointcut(pointcut);
                
                // Slightly odd side-effect here, but it's convenient.
                String parameterNamesString = (String)annot.getValue("argNames");
                this.setParameterNames(parameterNamesString.isEmpty() ? new String[0] : parameterNamesString.split(","));
                
                return new AdviceMirror(world, world.resolve(declaringType), kind, this, pc);
            }
        }
        
        return super.getAssociatedShadowMunger();
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
        
        return new MethodMirrorMember(world, method, declaringType, method.getModifiers(), returnType, method.getName(), parameterTypes, exceptionTypes);
    }
}
