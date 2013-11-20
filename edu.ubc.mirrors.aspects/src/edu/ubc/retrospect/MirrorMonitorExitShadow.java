package edu.ubc.retrospect;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorLocationEvent;
import edu.ubc.mirrors.ThreadMirror;

public class MirrorMonitorExitShadow extends MirrorEventShadow {

    private final MirrorLocationEvent event;
    private final InstanceMirror monitor;
    private final boolean isEntry;
    
    protected MirrorMonitorExitShadow(MirrorWorld world, MirrorLocationEvent event, InstanceMirror monitor, boolean isEntry, Shadow enclosingShadow) {
        super(world, event, Shadow.SynchronizationUnlock, MemberImpl.monitorExit(), enclosingShadow);
        this.event = event;
        this.isEntry = isEntry;
        this.monitor = monitor;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        MirrorMonitorExitShadow shadow = (MirrorMonitorExitShadow)other;
        return event.location().equals(shadow.event.location());
    }
    
    @Override
    public int hashCode() {
        return event.location().hashCode();
    }
    
    @Override
    public boolean isEntry() {
        return isEntry;
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
        return new MirrorEventVar(getArgType(i), monitor);
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
        return event.location().declaringClass();
    }
    
    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeSynchronizationStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.SYNCHRONIZATION_UNLOCK, monitor);
    }
}
