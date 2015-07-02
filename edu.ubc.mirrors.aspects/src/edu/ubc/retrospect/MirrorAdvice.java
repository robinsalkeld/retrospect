package edu.ubc.retrospect;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.Pointcut;

import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.Reflection;

public class MirrorAdvice extends Advice {
    private static final Member cflowCounterIncMethod = MemberImpl.method(NameMangler.CFLOW_COUNTER_UNRESOLVEDTYPE, 0,
            UnresolvedType.VOID, "inc", UnresolvedType.NONE);
    private static final Member cflowCounterDecMethod = MemberImpl.method(NameMangler.CFLOW_COUNTER_UNRESOLVEDTYPE, 0,
            UnresolvedType.VOID, "dec", UnresolvedType.NONE);
    private static final Member cflowCounterIsValidMethod = MemberImpl.method(NameMangler.CFLOW_COUNTER_UNRESOLVEDTYPE, 0,
            UnresolvedType.BOOLEAN, "isValid", UnresolvedType.NONE);

    private final MirrorWorld world;
    
    public MirrorAdvice(MirrorWorld world, AjAttribute.AdviceAttribute attribute, ResolvedType concreteAspect, Member signature, Pointcut pointcut) {
        super(attribute, pointcut, signature);
        this.world = world;
        this.concreteAspect = concreteAspect;
    }

    public MirrorAdvice(MirrorWorld world, ResolvedType concreteAspect, AdviceKind kind, Member signature, int extraArgumentFlags, Pointcut pointcut) {
        this(world, new AjAttribute.AdviceAttribute(kind, pointcut, extraArgumentFlags, pointcut.getStart(), pointcut.getEnd(), pointcut.getSourceContext()), concreteAspect, signature, pointcut);
    }

    public Object testAndExecute(MirrorEventShadow shadow, MirrorInvocationHandler proceed, List<Object> arguments) throws MirrorInvocationTargetException {
        ExposedState state = new ExposedState(signature);
        Test test = getPointcut().findResidue(shadow, state);
        MirrorEvaluator evaluator = shadow.getEvaluator(arguments);
        if (evaluator.evaluateTest(test)) {
            if (kind.isAfter()) {
                Object result = proceed.invoke(shadow.getThread(), arguments);
                if (getKind() == AdviceKind.AfterReturning) {
                    Var returnVar = new MirrorEventVar(world.resolve(shadow.getReturnType()), result);
                    state.set(0, returnVar);
                }
                execute(shadow, state, proceed, arguments);
                return result;
            } else if (getKind() == AdviceKind.Before) {
                execute(shadow, state, proceed, arguments);
                return proceed.invoke(shadow.getThread(), arguments);
            } else {
                return execute(shadow, state, proceed, arguments);
            }
        } else {
            return proceed.invoke(shadow.getThread(), arguments);
        }
    }

    private void cflowCounterEntry(MirrorEvaluator evaluator) {
        InstanceMirror counter = (InstanceMirror)evaluator.evaluateField(signature);
        evaluator.evaluateCall(counter, cflowCounterIncMethod, Expr.NONE);
        
        boolean isValid = (Boolean)evaluator.evaluateCall(counter, cflowCounterIsValidMethod, Expr.NONE);
        PointcutMirrorRequestExtractor.updateCflowGuardedRequestEnablement(signature, !isValid);
    }
    
    private void cflowCounterExit(MirrorEvaluator evaluator) {
        InstanceMirror counter = (InstanceMirror)evaluator.evaluateField(signature);
        evaluator.evaluateCall(counter, cflowCounterDecMethod, Expr.NONE);
        
        boolean isValid = (Boolean)evaluator.evaluateCall(counter, cflowCounterIsValidMethod, Expr.NONE);
        PointcutMirrorRequestExtractor.updateCflowGuardedRequestEnablement(signature, !isValid);
    }
    
    public Object executeCflow(MirrorEventShadow shadow, ExposedState state, MirrorInvocationHandler proceed, List<Object> arguments) throws MirrorInvocationTargetException {
        if (state.size() != 0) {
            throw new IllegalStateException("cflow with state not supported");
        }
        
        MirrorEvaluator evaluator = shadow.getEvaluator(arguments);
        AdviceKind adviceKind = shadow.adviceKind();
        if (adviceKind == AdviceKind.Around) {
            cflowCounterEntry(evaluator);
            try {
                return proceed.invoke(shadow.getThread(), arguments);
            } finally {
                cflowCounterExit(evaluator);
            }
        } else {
            if (adviceKind.isAfter()) {
                cflowCounterExit(evaluator);
            } else {
                cflowCounterEntry(evaluator);
            }
            return null;
        }
    }
    
    public Object execute(MirrorEventShadow shadow, ExposedState state, MirrorInvocationHandler proceed, List<Object> arguments) throws MirrorInvocationTargetException {
        world.showMessage(IMessage.DEBUG, shadow.toString(), null, null);
        world.showMessage(IMessage.DEBUG, signature.toString(), null, null);
        
        if (kind.isCflow()) {
            return executeCflow(shadow, state, proceed, arguments);
        }
        
        MirrorEvaluator evaluator = shadow.getEvaluator(arguments);
        
        InstanceMirror aspectInstance = (InstanceMirror)evaluator.evaluateExpr(state.getAspectInstance());
        Object[] args = new Object[state.size()];

        int baseArgCount = getBaseParameterCount();
        if (this.kind == AdviceKind.Around) {
            args[baseArgCount - 1] = world.makeInvocationHandlerAroundClosure(shadow.getThread(), proceed);
            baseArgCount--;
        }
        
        for (int i = 0; i < baseArgCount; i++) {
            args[i] = evaluator.evaluateExpr(state.get(i));
        }
        
        int extraArgIndex = args.length - 1;
        if ((getExtraParameterFlags() & Advice.ThisJoinPointStaticPart) != 0) {
            args[extraArgIndex--] = evaluator.evaluateExpr(shadow.getThisJoinPointStaticPartVar());
        }
        if ((getExtraParameterFlags() & Advice.ThisJoinPoint) != 0) {
            args[extraArgIndex--] = evaluator.evaluateExpr(shadow.getThisJoinPointVar());
        }
        if ((getExtraParameterFlags() & Advice.ThisEnclosingJoinPointStaticPart) != 0) {
            args[extraArgIndex--] = evaluator.evaluateExpr(shadow.getThisEnclosingJoinPointStaticPartVar());
        }
        
        // Entering adviceexecution()
        
        try {
            MethodMirrorMember member = (MethodMirrorMember)signature;
            Object result = member.method.invoke(shadow.getThread(), aspectInstance, args);
            
            // Leaving adviceexecution()
            
            // Autounboxing
            if (world.resolve(shadow.getReturnType()).isPrimitiveType() && (result instanceof InstanceMirror)) {
                return Reflection.unbox((InstanceMirror)result);
            } else {
                return result;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int compareTo(Object other) {
        // This is all copied directly from BcelAdvice - it seems to apply
        // perfectly well generically.
        if (!(other instanceof MirrorAdvice)) {
            return 0;
        }
        MirrorAdvice o = (MirrorAdvice) other;

        // System.err.println("compareTo: " + this + ", " + o);
        if (kind.getPrecedence() != o.kind.getPrecedence()) {
            if (kind.getPrecedence() > o.kind.getPrecedence()) {
                return +1;
            } else {
                return -1;
            }
        }

        if (kind.isCflow()) {
            // System.err.println("sort: " + this + " innerCflowEntries " + innerCflowEntries);
            // System.err.println("      " + o + " innerCflowEntries " + o.innerCflowEntries);
            boolean isBelow = (kind == AdviceKind.CflowBelowEntry);

            if (this.innerCflowEntries.contains(o)) {
                return isBelow ? +1 : -1;
            } else if (o.innerCflowEntries.contains(this)) {
                return isBelow ? -1 : +1;
            } else {
                return 0;
            }
        }

        if (kind.isPerEntry() || kind == AdviceKind.Softener) {
            return 0;
        }

        // System.out.println("compare: " + this + " with " + other);
        World world = concreteAspect.getWorld();

        int ret = concreteAspect.getWorld().compareByPrecedence(concreteAspect, o.concreteAspect);
        if (ret != 0) {
            return ret;
        }

        ResolvedType declaringAspect = getDeclaringAspect().resolve(world);
        ResolvedType o_declaringAspect = o.getDeclaringAspect().resolve(world);

        if (declaringAspect == o_declaringAspect) {
            if (kind.isAfter() || o.kind.isAfter()) {
                return this.getStart() < o.getStart() ? -1 : +1;
            } else {
                return this.getStart() < o.getStart() ? +1 : -1;
            }
        } else if (declaringAspect.isAssignableFrom(o_declaringAspect)) {
            return -1;
        } else if (o_declaringAspect.isAssignableFrom(declaringAspect)) {
            return +1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean hasDynamicTests() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void specializeOn(Shadow shadow) {
    }

    @Override
    public boolean implementOn(Shadow shadow) {
        ((MirrorEventShadow)shadow).implementAdvice(this);
        return true;
    }

    @Override
    public ShadowMunger parameterizeWith(ResolvedType declaringType, Map<String, UnresolvedType> typeVariableMap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ResolvedType> getThrownExceptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean mustCheckExceptions() {
        return false;
    }
    
//    @Override
//    public boolean match(Shadow shadow, World world) {
//        if (!super.match(shadow, world)) {
//            return false;
//        }
//        
//        if (kind.isCflow()) {
//            return true;
//        }
//        
//        MirrorEventShadow eventShadow = (MirrorEventShadow)shadow;
//        
//        if (kind == AdviceKind.AfterReturning && eventShadow.kind() == AdviceKind.After) {
//            return true;
//        }
//        
//        return eventShadow.kind() == kind;
//    }
}