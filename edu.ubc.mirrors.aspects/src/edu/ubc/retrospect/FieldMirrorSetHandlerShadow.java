package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirrorSetHandlerEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ThreadMirror;

public class FieldMirrorSetHandlerShadow extends MirrorEventShadow {
    
    FieldMirrorSetHandlerEvent event;
    
    public FieldMirrorSetHandlerShadow(MirrorWorld world, FieldMirrorSetHandlerEvent event, Member signature, Shadow enclosingShadow) {
        super(world, event, Shadow.FieldSet, signature, enclosingShadow);
        this.event = event;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        FieldMirrorSetHandlerShadow shadow = (FieldMirrorSetHandlerShadow)other;
        return event.field().equals(shadow.event.field());
    }

    @Override
    public int hashCode() {
        return event.field().hashCode();
    }

    @Override
    public AdviceKind kind() {
        return AdviceKind.Around;
    }

    @Override
    public ThreadMirror getThread() {
        return event.thread();
    }
    
    @Override
    public Var getThisVar() {
        return new MirrorEventVar(world.resolve(event.field().getDeclaringClass()), getThis());
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
        return new MirrorEventVar(getArgType(i), event.newValue());
    }
    
    @Override
    public ResolvedType getArgType(int arg) {
        return world.resolve(event.field().getType());
    }
    
    @Override
    protected ClassMirror getDeclaringClass() {
        return event.field().getDeclaringClass();
    }
    
    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.FIELD_SET, event.field());
    }
    
    @Override
    public Member getEnclosingCodeSignature() {
        return null;
    }
    
    @Override
    public String toString() {
        return getKind() + "(" + getSignature() + ")";
    }
}
