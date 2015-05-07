package edu.ubc.retrospect;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjcMemberMaker;
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
            String name, UnresolvedType[] parameterTypes) {
        super(Member.METHOD, declaringType, modifiers, returnType, name, parameterTypes);
        this.world = world;
        this.method = method;
    }

    public MethodMirror getMethod() {
        return method;
    }
    
    @Override
    public ShadowMunger getAssociatedShadowMunger() {
        for (AdviceKind kind : MirrorWorld.SUPPORTED_ADVICE_KINDS) {
            AnnotationMirror annot = Reflection.getAnnotation(method.getAnnotations(world.thread), world.getAnnotClassMirror(kind));
            if (annot != null) {
                // Slightly odd side-effect here, but it's convenient.
                String parameterNamesString = (String)annot.getValue(world.thread, "argNames");
                this.setParameterNames(parameterNamesString.isEmpty() ? new String[0] : parameterNamesString.split(","));
                
                String pointcut = null;
                if (kind == AdviceKind.AfterReturning) {
                    pointcut = (String)annot.getValue(world.thread, "pointcut");
                }
                if (pointcut == null) {
                    pointcut = (String)annot.getValue(world.thread, "value");
                }
                
                Pointcut pc = world.parsePointcut(pointcut);
                pc = world.resolvePointcut(this, pc);
                
                return new MirrorAdvice(world, world.resolve(declaringType), kind, this, extractExtraArgument(), pc);
            }
        }
        
        return super.getAssociatedShadowMunger();
    }
    
    private int extractExtraArgument() {
        int extraArgument = 0;
        for (String parameterTypeName : method.getParameterTypeNames()) {
            String signature = "L" + parameterTypeName.replace('.', '/') + ";";
            if (AjcMemberMaker.TYPEX_JOINPOINT.getSignature().equals(signature)) {
                    extraArgument |= Advice.ThisJoinPoint;
            } else if (AjcMemberMaker.TYPEX_PROCEEDINGJOINPOINT.getSignature().equals(signature)) {
                    extraArgument |= Advice.ThisJoinPoint;
            } else if (AjcMemberMaker.TYPEX_STATICJOINPOINT.getSignature().equals(signature)) {
                    extraArgument |= Advice.ThisJoinPointStaticPart;
            } else if (AjcMemberMaker.TYPEX_ENCLOSINGSTATICJOINPOINT.getSignature().equals(signature)) {
                    extraArgument |= Advice.ThisEnclosingJoinPointStaticPart;
            }
        }
        return extraArgument;
    }
    
    public static MethodMirrorMember make(MirrorWorld world, MethodMirror method) {
        UnresolvedType declaringType = world.resolve(method.getDeclaringClass());
        UnresolvedType returnType = UnresolvedType.forName(method.getReturnTypeName());
        
        UnresolvedType[] parameterTypes = new UnresolvedType[method.getParameterTypeNames().size()];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = UnresolvedType.forName(method.getParameterTypeNames().get(i));
        }
        
        return new MethodMirrorMember(world, method, declaringType, method.getModifiers(), returnType, method.getName(), parameterTypes);
    }
}
