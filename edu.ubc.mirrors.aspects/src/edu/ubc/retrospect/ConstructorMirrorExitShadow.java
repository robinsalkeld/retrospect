package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorMirrorExitShadow extends MirrorEventShadow {

    private final ConstructorMirrorExitEvent event;
    
    protected ConstructorMirrorExitShadow(MirrorWorld world, ConstructorMirrorExitEvent event, Member signature, Shadow enclosingShadow) {
        super(world, event, Shadow.ConstructorExecution, signature, enclosingShadow);
        this.event = event;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        ConstructorMirrorExitShadow shadow = (ConstructorMirrorExitShadow)other;
        return event.constructor().equals(shadow.event.constructor());
    }
    
    @Override
    public int hashCode() {
        return event.constructor().hashCode();
    }
    
    @Override
    public AdviceKind kind() {
        return AdviceKind.After;
    }
    
    @Override
    public ThreadMirror getThread() {
        return event.thread();
    }
    
    @Override
    public Var getThisVar() {
        return new MirrorEventVar(world.resolve(event.constructor().getDeclaringClass()), getThis());
    }

    @Override
    public Var getTargetVar() {
        return getThisVar();
    }

    @Override
    public int getArgCount() {
        return event.constructor().getParameterTypes().size();
    }
    
    @Override
    public Var getArgVar(int i) {
        return new MirrorEventVar(getArgType(i), getArgument(i));
    }
    
    @Override
    public ResolvedType getArgType(int arg) {
        return world.resolve(event.constructor().getParameterTypes().get(arg));
    }
    
    @Override
    protected ClassMirror getDeclaringClass() {
        return event.constructor().getDeclaringClass();
    }
    
    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.CONSTRUCTOR_EXECUTION, event.constructor());
    }
    
    @Override
    public Member getEnclosingCodeSignature() {
        return ConstructorMirrorMember.make(world, event.constructor());
    }
}
