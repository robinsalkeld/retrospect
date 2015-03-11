package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ThreadMirror;

public class MethodMirrorExecutionShadow extends MirrorEventShadow {
    
    private final MethodMirror method;
    private final ThreadMirror thread;
    private final MirrorInvocationHandler handler;
    
    public MethodMirrorExecutionShadow(MirrorWorld world, MethodMirror method, ThreadMirror thread, 
            MirrorInvocationHandler handler, Member signature, Shadow enclosingShadow) {
        super(world, null, Shadow.MethodExecution, signature, enclosingShadow);
        this.method = method;
        this.thread = thread;
        this.handler = handler;
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
    protected ClassMirror getDeclaringClass() {
        return method.getDeclaringClass();
    }

    @Override
    public AdviceKind kind() {
        return AdviceKind.Around;
    }

    @Override
    public ThreadMirror getThread() {
        return thread;
    }

    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.METHOD_EXECUTION, method);
    }
    
    public Var getAroundClosureVar() {
        return world.makeInvocationHandlerAroundClosureVar(handler);
    }

    @Override
    public String toString() {
        return getKind() + "(" + getSignature() + ")";
    }
}
