package edu.ubc.retrospect;

import java.util.List;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;
import org.aspectj.weaver.patterns.ExposedState;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirrorHandlerEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public class MethodMirrorExecutionShadow extends MirrorEventShadow {
    
    MethodMirrorHandlerEvent event;
    
    public MethodMirrorExecutionShadow(MirrorWorld world, MethodMirrorHandlerEvent event, Member signature, Shadow enclosingShadow) {
        super(world, event, Shadow.MethodExecution, signature, enclosingShadow);
        this.event = event;
    }

    @Override
    protected boolean equals(MirrorEventShadow other) {
        MethodMirrorExecutionShadow shadow = (MethodMirrorExecutionShadow)other;
        return event.method().equals(shadow.event.method());
    }

    @Override
    public int hashCode() {
        return event.method().hashCode();
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
        return new MirrorEventVar(world.resolve(event.method().getDeclaringClass()), getThis());
    }

    @Override
    public Var getTargetVar() {
        return getThisVar();
    }

    @Override
    public int getArgCount() {
        return event.method().getParameterTypes().size();
    }
    
    @Override
    public Var getArgVar(int i) {
        return new MirrorEventVar(getArgType(i), getArgument(i));
    }
    
    @Override
    public ResolvedType getArgType(int arg) {
        return world.resolve(event.method().getParameterTypes().get(arg));
    }
    
    @Override
    protected ClassMirror getDeclaringClass() {
        return event.method().getDeclaringClass();
    }
    
    @Override
    protected InstanceMirror getThisJoinPointStaticPart() {
        return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.METHOD_EXECUTION, event.method());
    }
    
    public Var getAroundClosureVar() {
        return world.makeInvocationHandlerAroundClosureVar(event.getProceed());
    }

    public void implementAdvice(final MirrorAdvice advice, ExposedState state) {
        MirrorInvocationHandler handler = new MirrorInvocationHandler() {
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                MirrorEvent newEvent = (MethodMirrorHandlerEvent)event.setProceed(event.getProceed(), args);
                setEvent(newEvent);
                
                return advice.testAndExecute(MethodMirrorExecutionShadow.this);
            }
        };
        setEvent(event.setProceed(handler, event.arguments()));
    }
    
    @Override
    public Object run() throws MirrorInvocationTargetException {
        return event.getProceed().invoke(event.thread(), event.arguments());
    }
    
    @Override
    public String toString() {
        return getKind() + "(" + getSignature() + ")";
    }
}
