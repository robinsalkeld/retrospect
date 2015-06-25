package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorMirrorExecutionShadow extends MirrorEventShadow {

    private final AdviceKind kind;
    private final ConstructorMirror constructor;
    private final ThreadMirror thread;
    
    protected ConstructorMirrorExecutionShadow(MirrorWorld world, Shadow.Kind shadowKind, AdviceKind kind, MirrorEvent event, 
            ConstructorMirror constructor, ThreadMirror thread, Member signature, Shadow enclosingShadow) {
        super(world, event, shadowKind, signature, enclosingShadow);
        this.kind = kind;
        this.constructor = constructor;
        this.thread = thread;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        ConstructorMirrorExecutionShadow shadow = (ConstructorMirrorExecutionShadow)other;
        return constructor.equals(shadow.constructor);
    }
    
    @Override
    public int hashCode() {
        return constructor.hashCode();
    }
    
    @Override
    public AdviceKind adviceKind() {
        return kind;
    }
    
    @Override
    public ThreadMirror getThread() {
        return thread;
    }
    
    @Override
    public Var getThisVar() {
        return new MirrorEventVar(world.resolve(constructor.getDeclaringClass()), getThis());
    }

    @Override
    public Var getTargetVar() {
        return getThisVar();
    }

    @Override
    public int getArgCount() {
        return constructor.getParameterTypes().size();
    }
    
    @Override
    public Var getArgVar(int i) {
        return new MirrorEventVar(getArgType(i), getArgument(i));
    }
    
    @Override
    public ResolvedType getArgType(int arg) {
        return world.resolve(constructor.getParameterTypes().get(arg));
    }
    
    @Override
    protected ClassMirror getDeclaringClass() {
        return constructor.getDeclaringClass();
    }
    
    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeStaticJoinPoint(getThread(), getKind().getName(), constructor);
    }
    
    @Override
    public Member getEnclosingCodeSignature() {
        return ConstructorMirrorMember.make(world, constructor);
    }
}
