package edu.ubc.retrospect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorMirrorEntryShadow extends MirrorEventShadow {

    private final ConstructorMirrorEntryEvent event;
    
    protected ConstructorMirrorEntryShadow(MirrorWorld world, ConstructorMirrorEntryEvent event, Member signature, Shadow enclosingShadow) {
        super(world, event, Shadow.ConstructorExecution, signature, enclosingShadow);
        this.event = event;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        ConstructorMirrorEntryShadow shadow = (ConstructorMirrorEntryShadow)other;
        return event.constructor().equals(shadow.event.constructor());
    }
    
    @Override
    public int hashCode() {
        return event.constructor().hashCode();
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
    public ResolvedType getEnclosingType() {
        return world.resolve(event.constructor().getDeclaringClass());
    }

    @Override
    public Var getThisJoinPointStaticPartVar() {
        ResolvedType joinPointStaticPartType = world.resolve(UnresolvedType.forName(JoinPoint.StaticPart.class.getName()));
        return new MirrorEventVar(joinPointStaticPartType, world.makeStaticJoinPoint(event));
    }
}
