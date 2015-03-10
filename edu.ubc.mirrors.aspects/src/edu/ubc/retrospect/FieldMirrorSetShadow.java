package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirrorSetEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ThreadMirror;

public class FieldMirrorSetShadow extends MirrorEventShadow {

    private final FieldMirrorSetEvent event;
    
    protected FieldMirrorSetShadow(MirrorWorld world, FieldMirrorSetEvent event, Member signature, Shadow enclosingShadow) {
        super(world, event, Shadow.FieldSet, signature, enclosingShadow);
        this.event = event;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        return event.field().equals(((FieldMirrorSetShadow)other).event.field());
    }
    
    @Override
    public int hashCode() {
        return event.field().hashCode();
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
    protected ClassMirror getDeclaringClass() {
        return getThread().getStackTrace().get(0).declaringClass();
    }
    
    @Override
    public Var getThisVar() {
        return new MirrorEventVar(getEnclosingType(), getThis());
    }

    @Override
    public Var getTargetVar() {
        return new MirrorEventVar(world.resolve(event.field().getType()), event.instance());
    }

    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.FIELD_SET, event.field());
    }
}
