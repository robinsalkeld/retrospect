package edu.ubc.retrospect;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.BindingScope;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.CflowPointcut;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.retrospect.MirrorWorld.AspectMirror;
import edu.ubc.retrospect.MirrorWorld.CflowStack;
import edu.ubc.retrospect.MirrorWorld.PointcutCallback;

// Used for pointcut declarations as well
public class AdviceMirror {
    private final MirrorWorld world;
    private final ClassMirror aspect;
    // Name of the pointcut or
    // Name of the advice - @AdviceName for code-style advice, method name for annotation-style advice
    private final String name;
    // @Before, @Pointcut, etc
    private final AdviceKind kind;
    // This holds the formal declarations
    private final MethodMirror methodMirror;
    private final String[] parameterNames;
    // Null for abstract pointcuts. Non-final because of the resolution phase.
    private Pointcut pc;
    
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
    
    public AdviceMirror(MirrorWorld world, ClassMirror aspect, AdviceKind kind, MethodMirror method, AnnotationMirror annotation) {
        this.world = world;
        this.aspect = aspect;
        this.kind = kind;
        this.name = getPointcutName(method);
        this.methodMirror = method;
        
        String parameterNamesString = (String)annotation.getValue("argNames");
        this.parameterNames = parameterNamesString.isEmpty() ? new String[0] : parameterNamesString.split(",");
        
        int pointcutFlags = method.getModifiers();
        
        if (Modifier.isAbstract(pointcutFlags)) {
            pc = null;
        } else {
            String pointcut = (String)annotation.getValue("value");
            pc = world.parsePointcut(pointcut);
        }
    }

    public Pointcut getPointcut() {
        return pc;
    }

    public AdviceKind getKind() {
        return kind;
    }
    
    public boolean isAbstract() {
        return pc == null;
    }
    
    private void resolve() {
        ResolvedType myType = world.resolve(aspect);
        FormalBinding[] formals = new FormalBinding[parameterNames.length];
        for (int i = 0; i < formals.length; i++) {
            UnresolvedType paramType = world.resolve(methodMirror.getParameterTypes().get(i));
            formals[i] = new FormalBinding(paramType, parameterNames[i], i);
        }
        ISourceContext context = new ISourceContext() {

            @Override
            public ISourceLocation makeSourceLocation(IHasPosition position) {
                return null;
            }

            @Override
            public ISourceLocation makeSourceLocation(int line, int offset) {
                return null;
            }

            @Override
            public int getOffset() {
                return 0;
            }

            @Override
            public void tidy() {
            }
            
        };
        BindingScope scope = new BindingScope(myType, context, formals);
        pc = pc.resolve(scope);
        
        pc = pc.concretize(myType, myType, formals.length);
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
                    ExposedState state = new ExposedState(MethodMirrorMember.make(world, methodMirror));
                    Test test = pc.findResidue(shadow, state);
                    if (MirrorTestEvaluator.evaluate(test)) {
                        callback.call(shadow, state);
                        break;
                    }
                }
                joinpointEvents.clear();
            }
        });
    }
    
    public void install(final MirrorReferenceTypeDelegate aspect) {
        resolve();
        
        MirrorEventRequestManager manager = world.vm.eventRequestManager();
        
        // Make sure the cflow trackers are pushed before normal advice, and popped
        // after.
        installCflows(manager, true, pc);
        installPointcutCallback(manager, kind, pc, new PointcutCallback() {
            public void call(MirrorEventShadow shadow, ExposedState state) {
                execute(aspect, shadow, state);
            }
        });
        installCflows(manager, false, pc);
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

    public void execute(MirrorReferenceTypeDelegate aspect, MirrorEventShadow shadow, ExposedState state) {
        InstanceMirror aspectInstance = aspect.getInstance();
        Object[] args = new Object[state.size()];

        for (int i = 0; i < args.length - 1; i++) {
            args[i] = ((MirrorEventVar)state.get(i)).getValue();
        }
        
        // TODO-RS: Actually check that we're passing in the right kind of join point,
        // if it's a parameter at all.
        args[args.length - 1] = ((MirrorEventVar)shadow.getThisJoinPointStaticPartVar()).getValue();

        try {
            methodMirror.invoke(shadow.getThread(), aspectInstance, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            // TODO-RS: Think about exceptions in general. Advice throwing exceptions
            // would perturb the original execution so they need to be handled specially.
            throw new RuntimeException(e);
        }

        return;
    }
}