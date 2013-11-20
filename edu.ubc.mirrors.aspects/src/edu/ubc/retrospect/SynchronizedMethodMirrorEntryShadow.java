package edu.ubc.retrospect;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.ThreadMirror;

public class SynchronizedMethodMirrorEntryShadow extends MirrorEventShadow {

    private final MethodMirrorEntryEvent event;
    
    protected SynchronizedMethodMirrorEntryShadow(MirrorWorld world, MethodMirrorEntryEvent event, Shadow enclosingShadow) {
        super(world, event, Shadow.SynchronizationLock, MemberImpl.monitorEnter(), enclosingShadow);
        this.event = event;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        SynchronizedMethodMirrorEntryShadow shadow = (SynchronizedMethodMirrorEntryShadow)other;
        return event.method().equals(shadow.event.method());
    }
    
    @Override
    public int hashCode() {
        return event.method().hashCode();
    }
    
    @Override
    public boolean isEntry() {
        return true;
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
        return world.resolve(getDeclaringClass());
    }
    
    @Override
    protected ClassMirror getDeclaringClass() {
        return event.method().getDeclaringClass();
    }
    
    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeSynchronizationStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.SYNCHRONIZATION_LOCK, getThis());
    }
}
