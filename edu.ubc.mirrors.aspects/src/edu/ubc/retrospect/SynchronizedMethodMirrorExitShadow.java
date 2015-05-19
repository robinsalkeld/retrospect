package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.ThreadMirror;

public class SynchronizedMethodMirrorExitShadow extends MirrorEventShadow {

    private final MethodMirrorExitEvent event;
    
    protected SynchronizedMethodMirrorExitShadow(MirrorWorld world, MethodMirrorExitEvent event, Shadow enclosingShadow) {
        super(world, event, Shadow.SynchronizationUnlock, MemberImpl.monitorExit(), enclosingShadow);
        this.event = event;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        SynchronizedMethodMirrorExitShadow shadow = (SynchronizedMethodMirrorExitShadow)other;
        return event.method().equals(shadow.event.method());
    }
    
    @Override
    public int hashCode() {
        return event.method().hashCode();
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
        return new MirrorEventVar(world.resolve(getDeclaringClass()), getThis());
    }

    @Override
    public Var getTargetVar() {
        return getThisVar();
    }

    @Override
    public int getArgCount() {
        return 1;
    }
    
    @Override
    public Var getArgVar(int i) {
        return new MirrorEventVar(getArgType(i), getThis());
    }
    
    @Override
    public ResolvedType getArgType(int arg) {
        if (arg != 0) {
            throw new IllegalArgumentException();
        }
        return world.resolve(UnresolvedType.OBJECT);
    }
    
    @Override
    protected ClassMirror getDeclaringClass() {
        return event.method().getDeclaringClass();
    }
    
    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeSynchronizationStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.SYNCHRONIZATION_UNLOCK, getThis());
    }
    
    @Override
    public Member getEnclosingCodeSignature() {
        return MethodMirrorMember.make(world, event.method());
    }
}
