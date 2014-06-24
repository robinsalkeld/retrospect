package edu.ubc.retrospect;

import java.util.List;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorMirrorExecutionShadow extends MirrorEventShadow {

    private final ConstructorMirror constructor;
    private final ThreadMirror thread;
    
    protected ConstructorMirrorExecutionShadow(MirrorWorld world, ConstructorMirrorEntryEvent event, Shadow enclosingShadow) {
        super(world, event, Shadow.ConstructorExecution, ConstructorMirrorMember.make(world, event.constructor()), enclosingShadow);
        this.constructor = event.constructor();
        this.thread = event.thread();
    }
    
    protected ConstructorMirrorExecutionShadow(MirrorWorld world, ConstructorMirrorExitEvent event, Shadow enclosingShadow) {
        super(world, event, Shadow.ConstructorExecution, ConstructorMirrorMember.make(world, event.constructor()), enclosingShadow);
        this.constructor = event.constructor();
        this.thread = event.thread();
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
    public boolean isEntry() {
        return event instanceof ConstructorMirrorEntryEvent;
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
        return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.CONSTRUCTOR_EXECUTION, constructor);
    }
    
    @Override
    public Object proceedManually(List<Object> arguments) {
        throw new UnsupportedOperationException();
    }
}
