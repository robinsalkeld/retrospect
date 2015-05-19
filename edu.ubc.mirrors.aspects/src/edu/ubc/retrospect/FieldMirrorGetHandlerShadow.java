package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirrorGetHandlerEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ThreadMirror;

public class FieldMirrorGetHandlerShadow extends MirrorEventShadow {
    
    FieldMirrorGetHandlerEvent event;
    
    public FieldMirrorGetHandlerShadow(MirrorWorld world, FieldMirrorGetHandlerEvent event, Member signature, Shadow enclosingShadow) {
        super(world, event, Shadow.FieldGet, signature, enclosingShadow);
        this.event = event;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        FieldMirrorGetHandlerShadow shadow = (FieldMirrorGetHandlerShadow)other;
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
        return 0;
    }
    
    @Override
    public Var getArgVar(int i) {
        throw new IllegalArgumentException();
    }
    
    @Override
    public ResolvedType getArgType(int arg) {
        throw new IllegalArgumentException();
    }
    
    @Override
    protected ClassMirror getDeclaringClass() {
        return event.field().getDeclaringClass();
    }
    
    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.FIELD_GET, event.field());
    }
    
    @Override
    public String toString() {
        return getKind() + "(" + getSignature() + ")";
    }
    
    @Override
    public Member getEnclosingCodeSignature() {
        return null;
    }
}
