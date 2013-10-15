package edu.ubc.retrospect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BindingScope;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.ast.CallExpr;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.CflowPointcut;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;

import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.retrospect.MirrorWorld.CflowStack;
import edu.ubc.retrospect.MirrorWorld.PointcutCallback;

public class AdviceMirror extends Advice {
    private final MirrorWorld world;
    
    public static String getPointcutName(MethodMirror method) {
        String name = method.getName();
        // Stolen from org.aspectj.internal.lang.reflect.AjTypeImpl#asPointcut(Method)
        if (name.startsWith("ajc$")) {
            // extract real name
            int nameStart = name.indexOf("$$");
            name = name.substring(nameStart +2,name.length());
            int nextDollar = name.indexOf("$");
            if (nextDollar != -1) name = name.substring(0,nextDollar);
        }
        return name;
    }
    
    public AdviceMirror(MirrorWorld world, AjAttribute.AdviceAttribute attribute, ResolvedType concreteAspect, Member signature, Pointcut pointcut) {
        super(attribute, pointcut, signature);
        this.world = world;
        this.concreteAspect = concreteAspect;
    }

    public AdviceMirror(MirrorWorld world, ResolvedType concreteAspect, AdviceKind kind, Member signature, Pointcut pointcut) {
        this(world, new AjAttribute.AdviceAttribute(kind, pointcut, 0, pointcut.getStart(), pointcut.getEnd(), pointcut.getSourceContext()), concreteAspect, signature, pointcut);
    }

    private void resolve() {
        String[] parameterNames = signature.getParameterNames(world);
        FormalBinding[] formals = new FormalBinding[parameterNames.length];
        for (int i = 0; i < formals.length; i++) {
            UnresolvedType paramType = signature.getParameterTypes()[i];
            formals[i] = new FormalBinding(paramType, parameterNames[i], i);
        }
        BindingScope scope = new BindingScope(concreteAspect, pointcut.getSourceContext(), formals);
        pointcut = pointcut.resolve(scope);
        
        pointcut = pointcut.concretize(concreteAspect, concreteAspect, formals.length);
    }
    
    private void installPointcutCallback(MirrorEventRequestManager manager, final AdviceKind kind, final Pointcut pc, final PointcutCallback callback) {
        Pointcut dnf = new PointcutRewriter().rewrite(pc);
        List<Pointcut> actualDNF = disjuncts(dnf);
        final Set<MirrorEvent> joinpointEvents = new HashSet<MirrorEvent>();
        
        for (Pointcut disjunct : actualDNF) {
            MirrorEventRequest request = extractRequest(manager, kind, disjunct);
            
            world.vm.dispatch().addCallback(request, new EventDispatch.EventCallback() {
                @Override
                public void handle(MirrorEvent event) {
                    joinpointEvents.add(event);
                }
            });
            request.enable();
        }
    
        world.vm.dispatch().addSetCallback(new Runnable() {
            @Override
            public void run() {
                // Note the return below - each pointcut should only apply at most once to
                // each joinpoint, even if there are multiple events at that joinpoint
                // that match.
                for (MirrorEvent event : joinpointEvents) {
                    MirrorEventShadow shadow = MirrorEventShadow.make(world, event);
                    ExposedState state = new ExposedState(signature);
                    Test test = pc.findResidue(shadow, state);
                    if (shadow.evaluateTest(test)) {
                        callback.call(shadow, state);
                        break;
                    }
                }
                joinpointEvents.clear();
            }
        });
    }
    
    public void install() {
        MirrorEventRequestManager manager = world.vm.eventRequestManager();
        
        // Make sure the cflow trackers are pushed before normal advice, and popped
        // after.
        installCflows(manager, true, pointcut);
        installPointcutCallback(manager, kind, pointcut, new PointcutCallback() {
            public void call(MirrorEventShadow shadow, ExposedState state) {
                execute(shadow, state);
            }
        });
        installCflows(manager, false, pointcut);
    }
    
    private CflowStack getCflowStack(Pointcut pc) {
        CflowStack stack = world.cflowStacks.get(pc);
        if (!world.cflowStacks.containsKey(pc)) {
            stack = new CflowStack();
            world.cflowStacks.put(pc, stack);
        }
        return stack;
    }
    
    private void installCflows(MirrorEventRequestManager manager, final boolean before, final Pointcut pc) {
        if (pc instanceof CflowPointcut) {
            final CflowStack stack = getCflowStack(pc);
            final Pointcut child = ((CflowPointcut)pc).getEntry();
            AdviceKind kind = before ? AdviceKind.Before : AdviceKind.After;
            installPointcutCallback(manager, kind, child, new PointcutCallback() {
                public void call(MirrorEventShadow shadow, ExposedState state) {
                    ThreadMirror thread = shadow.getThread();
                    if (before) {
                        stack.pushState(thread, state);
                    } else {
                        stack.popState(thread);
                    }
                }
            });
            
        } else if (pc instanceof AndPointcut) {
            AndPointcut andPC = (AndPointcut)pc;
            installCflows(manager, before, andPC.getLeft());
            installCflows(manager, before, andPC.getRight());
        } else if (pc instanceof OrPointcut) {
            OrPointcut orPC = (OrPointcut)pc;
            installCflows(manager, before, orPC.getLeft());
            installCflows(manager, before, orPC.getRight());
        } else if (pc instanceof NotPointcut) {
            installCflows(manager, before, ((NotPointcut)pc).getNegatedPointcut());
        }
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

    private List<Pointcut> conjuncts(Pointcut pc) {
        if (pc instanceof AndPointcut) {
            AndPointcut or = (AndPointcut)pc;
            List<Pointcut> result = new ArrayList<Pointcut>();
            result.addAll(conjuncts(or.getLeft()));
            result.addAll(conjuncts(or.getRight()));
            return result;
        } else {
            List<Pointcut> result = new ArrayList<Pointcut>(1);
            result.add(pc);
            return result;
        }
    }

    private MirrorEventRequest extractRequest(MirrorEventRequestManager manager, AdviceKind adviceKind, Pointcut disjunct) {
        return PointcutMirrorRequestExtractor.extractRequest(world.vm, adviceKind, disjunct);
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasDynamicTests() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void specializeOn(Shadow shadow) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean implementOn(Shadow shadow) {
        // TODO Auto-generated method stub
        return false;
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
}