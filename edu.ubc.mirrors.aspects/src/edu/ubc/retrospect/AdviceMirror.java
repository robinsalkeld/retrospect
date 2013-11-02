package edu.ubc.retrospect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import org.aspectj.weaver.ast.FieldGet;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorInvocationTargetException;

public class AdviceMirror extends Advice {
    private static final Member cflowCounterIncMethod = MemberImpl.method(NameMangler.CFLOW_COUNTER_UNRESOLVEDTYPE, 0,
            UnresolvedType.VOID, "inc", UnresolvedType.NONE);
    private static final Member cflowCounterDecMethod = MemberImpl.method(NameMangler.CFLOW_COUNTER_UNRESOLVEDTYPE, 0,
            UnresolvedType.VOID, "dec", UnresolvedType.NONE);

    private final MirrorWorld world;
    
    public AdviceMirror(MirrorWorld world, AjAttribute.AdviceAttribute attribute, ResolvedType concreteAspect, Member signature, Pointcut pointcut) {
        super(attribute, pointcut, signature);
        this.world = world;
        this.concreteAspect = concreteAspect;
    }

    public AdviceMirror(MirrorWorld world, ResolvedType concreteAspect, AdviceKind kind, Member signature, Pointcut pointcut) {
        this(world, new AjAttribute.AdviceAttribute(kind, pointcut, 0, pointcut.getStart(), pointcut.getEnd(), pointcut.getSourceContext()), concreteAspect, signature, pointcut);
    }

    // Collect DNF assuming the argument is already a DNF pointcut
    private List<Pointcut> disjuncts(Pointcut pc) {
        if (pc instanceof OrPointcut) {
            OrPointcut or = (OrPointcut)pc;
            List<Pointcut> result = new ArrayList<Pointcut>();
            result.addAll(disjuncts(or.getLeft()));
            result.addAll(disjuncts(or.getRight()));
            return result;
        } else {
            List<Pointcut> result = new ArrayList<Pointcut>(1);
            result.add(pc);
            return result;
        }
    }

    void installCallback(Callback<MirrorEvent> callback) {
        Pointcut dnf = new PointcutRewriter().rewrite(pointcut);
        PointcutMirrorRequestExtractor.installCallback(world, kind, dnf, callback);
    }

    public void execute(MirrorEventShadow shadow, ExposedState state) {
        InstanceMirror aspectInstance = (InstanceMirror)shadow.evaluateExpr(state.getAspectInstance());
        Object[] args = new Object[state.size()];

        for (int i = 0; i < args.length - 1; i++) {
            args[i] = shadow.evaluateExpr(state.get(i));
        }
        
        // TODO-RS: Actually check that we're passing in the right kind of join point,
        // if it's a parameter at all.
        args[args.length - 1] = shadow.evaluateExpr(shadow.getThisJoinPointStaticPartVar());;

        try {
            MethodMirrorMember member = (MethodMirrorMember)signature;
            member.method.invoke(shadow.getThread(), aspectInstance, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            // TODO-RS: Think about exceptions in general. Advice throwing exceptions
            // would perturb the original execution so they need to be handled specially.
            throw new RuntimeException(e);
        }

        return;
    }

    @Override
    public int compareTo(Object other) {
        // This is all copied directly from BcelAdvice - it seems to apply
        // perfectly well generically.
        if (!(other instanceof AdviceMirror)) {
            return 0;
        }
        AdviceMirror o = (AdviceMirror) other;

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
        ExposedState state = new ExposedState(signature);
        MirrorEventShadow eventShadow = (MirrorEventShadow)shadow;
        Test test = getPointcut().findResidue(shadow, state);
        if (eventShadow.evaluateTest(test)) {
            if (getSignature() instanceof MethodMirrorMember) {
                execute(eventShadow, state);
            } else if (kind.isCflow()) {
                if (state.size() == 0) {
                    Expr fieldGet = new FieldGet(getSignature(), concreteAspect);
                    InstanceMirror counter = (InstanceMirror)eventShadow.evaluateExpr(fieldGet);
                    Member method = eventShadow.isEntry() ? cflowCounterIncMethod : cflowCounterDecMethod;
                    eventShadow.evaluateCall(counter, method, Expr.NONE);
                } else {
                    throw new IllegalStateException();
                }
            }
            return true;
        } else {
            return false;
        }
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
    
    @Override
    public boolean match(Shadow shadow, World world) {
        if (!super.match(shadow, world)) {
            return false;
        }
        
        if (kind.isCflow()) {
            return true;
        }
        
        MirrorEventShadow eventShadow = (MirrorEventShadow)shadow;
        if (eventShadow.isEntry()) {
            return !kind.isAfter();
        } else {
            return kind.isAfter();
        }
    }
}