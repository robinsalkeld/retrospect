package edu.ubc.retrospect;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorLocationEvent;
import edu.ubc.mirrors.ThreadMirror;

public class MirrorMonitorEnterShadow extends MirrorEventShadow {

    private final MirrorLocationEvent event;
    private final boolean isEntry;
    
    protected MirrorMonitorEnterShadow(MirrorWorld world, MirrorLocationEvent event, boolean isEntry, Member signature, Shadow enclosingShadow) {
        super(world, event, Shadow.SynchronizationLock, signature, enclosingShadow);
        this.event = event;
        this.isEntry = isEntry;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        MirrorMonitorEnterShadow shadow = (MirrorMonitorEnterShadow)other;
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
        // TODO-RS: Figure out how to get argument off the stack.
        return new MirrorEventVar(getArgType(i), null);
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
        return event.location().declaringClass();
    }
}