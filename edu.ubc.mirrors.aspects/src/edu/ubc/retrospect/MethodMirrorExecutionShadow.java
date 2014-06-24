package edu.ubc.retrospect;

import java.util.List;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.ThreadMirror;

public class MethodMirrorExecutionShadow extends MirrorEventShadow {

    private final MethodMirror method;
    private final ThreadMirror thread;
    
    protected MethodMirrorExecutionShadow(MirrorWorld world, MethodMirrorEntryEvent event, Shadow enclosingShadow) {
        super(world, event, Shadow.MethodExecution, MethodMirrorMember.make(world, event.method()), enclosingShadow);
        this.method = event.method();
        this.thread = event.thread();
    }

    protected MethodMirrorExecutionShadow(MirrorWorld world, MethodMirrorExitEvent event, Shadow enclosingShadow) {
        super(world, event, Shadow.MethodExecution, MethodMirrorMember.make(world, event.method()), enclosingShadow);
        this.method = event.method();
        this.thread = event.thread();
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        MethodMirrorExecutionShadow shadow = (MethodMirrorExecutionShadow)other;
        return method.equals(shadow.method);
    }
    
    @Override
    public int hashCode() {
        return method.hashCode();
    }
    
    @Override
    public boolean isEntry() {
        return event instanceof MethodMirrorEntryEvent;
    }
    
    @Override
    public ThreadMirror getThread() {
        return thread;
    }
    
    @Override
    public Var getThisVar() {
        return new MirrorEventVar(world.resolve(method.getDeclaringClass()), getThis());
    }

    @Override
    public Var getTargetVar() {
        return getThisVar();
    }

    @Override
    public int getArgCount() {
        return method.getParameterTypes().size();
    }
    
    @Override
    public Var getArgVar(int i) {
        return new MirrorEventVar(getArgType(i), getArgument(i));
    }
    
    @Override
    public ResolvedType getArgType(int arg) {
        return world.resolve(method.getParameterTypes().get(arg));
    }
    
    @Override
    protected ClassMirror getDeclaringClass() {
        return method.getDeclaringClass();
    }
    
    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.METHOD_EXECUTION, method);
    }
    
    @Override
    public Object proceedManually(List<Object> arguments) {
        throw new UnsupportedOperationException();
    }
}
