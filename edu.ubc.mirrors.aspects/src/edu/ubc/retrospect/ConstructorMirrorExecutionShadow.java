package edu.ubc.retrospect;

import java.util.concurrent.Callable;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

public class ConstructorMirrorExecutionShadow extends MirrorEventShadow {

    private final AdviceKind kind;
    private final ConstructorMirror constructor;
    private final ThreadMirror thread;
    private final MirrorInvocationHandler handler;
    
    protected ConstructorMirrorExecutionShadow(MirrorWorld world, AdviceKind kind, ConstructorMirror constructor, ThreadMirror thread, 
            MirrorInvocationHandler handler, Member signature, Shadow enclosingShadow) {
        super(world, null, Shadow.ConstructorExecution, signature, enclosingShadow);
        this.kind = kind;
        this.constructor = constructor;
        this.thread = thread;
        this.handler = handler;
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
    public AdviceKind kind() {
        return kind;
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
    
    public Var getAroundClosureVar() {
        return Reflection.withThread(thread, new Callable<Var>() {
            public Var call() throws Exception {
                ResolvedType aroundClosureType = world.resolve(world.getAroundClosureClass());
                InstanceMirror closure = world.getAroundClosureClass().newRawInstance();
                InstanceMirror closureWrapper =  new AroundClosureMirror(closure, handler);
                return new MirrorEventVar(aroundClosureType, closureWrapper);
            }
        });
    }
}
