package edu.ubc.retrospect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.apache.bcel.classfile.ClassFormatException;
import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.ArgsPointcut;
import org.aspectj.weaver.patterns.ConcreteCflowPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.PatternNode;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.WithinPointcut;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationEvent;
import edu.ubc.mirrors.MirrorLocationRequest;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;

/**
 * A visitor that installs the most specific possible event requests
 * that over-approximates all events that might match a given pointcut.
 * That is, it will install requests such that a given callback will be
 * invoked for a superset of the Shadows that match a given pointcut.
 * 
 * @author Robin Salkeld
 *
 */
public class PointcutMirrorRequestExtractor {

    private static class RequestContext {
        int kinds = Shadow.ALL_SHADOW_KINDS_BITS;
        final List<PatternNode> thisFilters = new ArrayList<PatternNode>();
        final List<PatternNode> targetFilters = new ArrayList<PatternNode>();
        private Member cflowCounterGuard = null;
    }
    
    public static final Object SHADOW_KIND_PROPERTY_KEY = PointcutMirrorRequestExtractor.class.getName() + ".shadowKind";
    
    private final Advice advice;
    private final MirrorWorld world;
    private final Callback<MirrorEvent> callback;
    private final MirrorEventRequestManager manager;
    
    private static final Map<Member, List<MirrorEventRequest>> cflowGuardedRequests = 
            new HashMap<Member, List<MirrorEventRequest>>();
    
    public static void updateCflowGuardedRequestEnablement(Member cflowCounterMember, boolean enable) {
        for (MirrorEventRequest request : cflowGuardedRequests.get(cflowCounterMember)) {
            request.setEnabled(enable);
        }
    }
    
    public PointcutMirrorRequestExtractor(final MirrorWorld world, Advice advice) {
        this.advice = advice;
        this.world = world;
        this.callback = world.eventCallback();
        this.manager = world.vm.eventRequestManager();
    }
    
    public Object visit(Pointcut node, Object parent, RequestContext context) {
        if (node instanceof AndPointcut) {
            return visit((AndPointcut)node, parent, context);
        } else if (node instanceof OrPointcut) {
            return visit((OrPointcut)node, parent, context);
        } else if (node instanceof NotPointcut) {
            return visit((NotPointcut)node, parent, context);
        } else if (node instanceof WithinPointcut) {
            return visit((WithinPointcut)node, parent, context);
        } else if (node instanceof KindedPointcut) {
            return visit((KindedPointcut)node, parent, context);
        } else if (node instanceof ConcreteCflowPointcut) {
            return visit((ConcreteCflowPointcut)node, parent, context);
        } else if (node instanceof ThisOrTargetPointcut || node instanceof ArgsPointcut) {
            // Could possibly add filters to match the type pattern...
            return null;
        } else if (node instanceof PerClause) {
            // Ignore pointcuts with no scope
            return null;
        } else {
            throw new UnsupportedOperationException("Unsupported pointcut type: " + node.getClass());
        }
    }
    
    public Object visit(WithinPointcut node, Object parent, RequestContext context) {
        context.kinds &= node.couldMatchKinds();
        context.thisFilters.add(node.getTypePattern());
        installIfNotBelowAnd(parent, context);
        return null;
    }
    
    public Object visit(KindedPointcut node, Object parent, RequestContext context) {
        context.kinds &= node.couldMatchKinds();
        if (node.getKind().bit == Shadow.FieldGetBit
                || node.getKind().bit == Shadow.FieldSetBit) {
            context.targetFilters.add(node.getSignature());
        } else {
            context.thisFilters.add(node.getSignature());
        }
        installIfNotBelowAnd(parent, context);
        return null;
    }
    
    public Object visit(OrPointcut node, Object parent, RequestContext context) {
        visit(node.getLeft(), node, context);
        context = new RequestContext();
        visit(node.getRight(), node, context);

        return null;
    }
    
    public Object visit(AndPointcut node, Object parent, RequestContext context) {
        visit(node.getLeft(), node, context);
        visit(node.getRight(), node, context);
        installIfNotBelowAnd(parent, context);
        return null;
    }
    
    public Object visit(NotPointcut node, Object parent, RequestContext context) {
        if (parent instanceof AndPointcut) {
            if (node.getNegatedPointcut() instanceof ConcreteCflowPointcut) {
                ConcreteCflowPointcut negated = (ConcreteCflowPointcut)node.getNegatedPointcut();
                visit(negated, node, context);
            }
            
            // Safe to just drop any nots underneath ands since the requests will
            // still cover a superset of the correct events.
            return null;
        } else {
            // Could implement by negating kinds and installing no filters,
            // but that would be very inefficient and realistically never used.
            throw new IllegalArgumentException("Not pointcuts that aren't under Ands not supported");
        }
    }
    
    private static final Field cflowFieldField;
    static {
        try {
            cflowFieldField = ConcreteCflowPointcut.class.getDeclaredField("cflowField");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        cflowFieldField.setAccessible(true);
    }
    
    public Object visit(ConcreteCflowPointcut node, Object parent, RequestContext context) {
        if (parent instanceof NotPointcut) {
            if (context.cflowCounterGuard != null) {
                throw new IllegalArgumentException("More than one cflow per pointcut is not supported");
            }
            
            // Cheating to extract the counter field since it's not exposed :(
            try {
                context.cflowCounterGuard = (Member)cflowFieldField.get(node);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        return null;
    }
    
    private void installIfNotBelowAnd(Object parent, RequestContext context) {
        if (!(parent instanceof AndPointcut)) {
            installRequest(context);
        }
    }
    
    private void installRequest(RequestContext context) {
        AdviceKind adviceKind = advice.getKind();
        for (final Shadow.Kind kind : Shadow.toSet(context.kinds)) {
            switch (kind.bit) {
            case (Shadow.MethodExecutionBit):
            case (Shadow.MethodCallBit):
                // If there are no filters at all, it's much more efficient to create single request
                if (context.thisFilters.isEmpty()) {
                    if (adviceKind == AdviceKind.Before || adviceKind.isCflow()) {
                        addFiltersAndInstall(manager.createMethodMirrorHandlerRequest(), context.thisFilters, kind, context);
                    }
                    if (adviceKind.isAfter()) {
                        addFiltersAndInstall(manager.createMethodMirrorHandlerRequest(), context.thisFilters, kind, context);
                    }
                    if (adviceKind == AdviceKind.Around) {
                        addFiltersAndInstall(manager.createMethodMirrorHandlerRequest(), context.thisFilters, kind, context);
                    }
                } else {
                    installMethodRequests(kind, context);
                }
                break;
            case (Shadow.ConstructorExecutionBit):
            case (Shadow.ConstructorCallBit):
                if (adviceKind == AdviceKind.Before || adviceKind.isCflow()) {
                    addFiltersAndInstall(manager.createConstructorMirrorHandlerRequest(), context.thisFilters, kind, context);
                }
                if (adviceKind.isAfter()) {
                    addFiltersAndInstall(manager.createConstructorMirrorHandlerRequest(), context.thisFilters, kind, context);
                }
                if (adviceKind == AdviceKind.Around) {
                    addFiltersAndInstall(manager.createConstructorMirrorHandlerRequest(), context.thisFilters, kind, context);
                }
                break;
            case (Shadow.FieldSetBit):
            case (Shadow.FieldGetBit):
                installFieldRequests(kind, context);
                break;
            case Shadow.SynchronizationLockBit:
            case Shadow.SynchronizationUnlockBit:
                installSynchronizationRequests(kind, context);
                break;
            case Shadow.AdviceExecutionBit:
            case Shadow.InitializationBit:
            case Shadow.PreInitializationBit:
            case Shadow.ExceptionHandlerBit:
                world.showMessage(IMessage.WARNING, "Unsupported pointcut kind: " + kind, null, null);
                break;
            }
        }
    }
    
    private void forAllWovenClasses(final Callback<ClassMirror> callback) {
        world.vm.dispatch().forAllClasses(new Callback<ClassMirror>() {
            public ClassMirror handle(ClassMirror klass) {
                if (!MirrorWorld.weaveClass(klass.getClassName())) {
                    return klass;
                } else {
                    return callback.handle(klass);
                }
            }
        });
    }
    
    private void installFieldRequests(final Shadow.Kind kind, final RequestContext context) {
        // No point in tracking cflow state in these, since get()/set() joinpoints
        // never contain any other joinpoints.
        if (advice.getKind().isCflow()) {
            return;
        }
        
        // TODO-RS: Optimize for the exact field match case
        
        // Copy the state to make sure the class prepare callback
        // reads the right state.
        forAllWovenClasses(new Callback<ClassMirror>() {
            public ClassMirror handle(ClassMirror klass) {
                ReferenceType type = (ReferenceType)world.resolve(klass);
                for (ResolvedMember field : type.getDeclaredFields()) {
                    boolean matches = true;
                    for (PatternNode targetPattern : context.targetFilters) {
                        if (targetPattern instanceof SignaturePattern) {
                            if (!(((SignaturePattern)targetPattern).matches(field, world, false))) {
                                matches = false;
                                break;
                            }
                        }
                    }
                    if (matches) {
                        FieldMirrorMember member = (FieldMirrorMember)field;
                        MirrorEventRequest request;
                        FieldMirror fieldMirror = member.getField();
                        if (advice.getKind() == AdviceKind.Around) {
                            if (kind.bit == Shadow.FieldSetBit) {
                                request = manager.createFieldMirrorSetHandlerRequest(fieldMirror.getDeclaringClass().getClassName(),
                                        fieldMirror.getName());
                            } else {
                                request = manager.createFieldMirrorGetHandlerRequest(fieldMirror.getDeclaringClass().getClassName(),
                                        fieldMirror.getName());
                            }
                        } else if (advice.getKind() == AdviceKind.Before) {
                            if (kind.bit == Shadow.FieldSetBit) {
                                request = manager.createFieldMirrorSetRequest(fieldMirror.getDeclaringClass().getClassName(),
                                        fieldMirror.getName());
                            } else {
                                request = manager.createFieldMirrorGetRequest(fieldMirror.getDeclaringClass().getClassName(),
                                        fieldMirror.getName());
                            }
                        } else {
                            throw new IllegalArgumentException("After advice on field get/set not supported");
                        }
                        addFiltersAndInstall(request, context.targetFilters, kind, context);
                    }
                }
                
                return klass;
            };
        });
    }
    
    private void installSynchronizationRequests(final Shadow.Kind kind, final RequestContext context) {
        // No point in tracking cflow state in these, since lock()/unlock() joinpoints
        // never contain any other joinpoints.
        if (advice.getKind().isCflow()) {
            return;
        }
        
        
        // TODO-RS: This isn't perfect, since I'm only supporting execution() shadows
        // at the moment, which implies that only after: lock() and before: unlock() work.
        // Realistically it doesn't matter unless the code queries the thread state in very specific ways.
        forAllWovenClasses(new Callback<ClassMirror>() {
            public ClassMirror handle(ClassMirror klass) {
                ReferenceType type = (ReferenceType)world.resolve(klass);
                
                // These are handled in two parts:
                // 1. Delegate to handling the pointcut call(synchronized * *.*(..)), to 
                //    catch all synchronized methods.
                for (ResolvedMember method : type.getDeclaredMethods()) {
                    if (Modifier.isSynchronized(method.getModifiers())) {
                        MethodMirrorMember member = (MethodMirrorMember)method;
                        MirrorEventRequest request;
                        MethodMirror methodMirror = (MethodMirror)member.getMethod();
                        
                        if (advice.getKind() == AdviceKind.Before && kind == Shadow.SynchronizationLock) {
                            MethodMirrorEntryRequest mmer = manager.createMethodMirrorEntryRequest();
                            mmer.setMethodFilter(methodMirror.getDeclaringClass().getClassName(), 
                                    methodMirror.getName(), methodMirror.getParameterTypeNames());
                            request = mmer;
                        } else if (advice.getKind().isAfter() && kind == Shadow.SynchronizationUnlock) {
                            MethodMirrorExitRequest mmer = manager.createMethodMirrorExitRequest();
                            mmer.setMethodFilter(methodMirror.getDeclaringClass().getClassName(),
                                    methodMirror.getName(), methodMirror.getParameterTypeNames());
                            request = mmer;
                        } else {
                            throw new IllegalArgumentException("Unsupported lock()/unlock() advice kind: " + advice);
                        }
                        addFiltersAndInstall(request, context.thisFilters, kind, context);
                    }
                }
                
                // 2. Search through the bytecode for MONITORENTER/EXIT instructions,
                //    and create breakpoints for each.
                try {
                    Map<MethodMirror, Method> bcelMethods = null;
                    for (MethodMirror methodMirror : klass.getDeclaredMethods(false)) {
                        int modifiers = methodMirror.getModifiers();
                        if (Modifier.isNative(modifiers)
                                || Modifier.isAbstract(modifiers)
                                || (modifiers & Opcodes.ACC_BRIDGE) != 0) {
                            continue;
                        }
                        
                        byte[] bytecode;
                        try {
                            bytecode = methodMirror.getBytecode(); 
                        } catch (UnsupportedOperationException e) {
                            if (bcelMethods == null) {
                                bcelMethods = new HashMap<MethodMirror, Method>();
                                JavaClass bcelClass = new ClassParser(new ByteArrayInputStream(klass.getBytecode()), "").parse();
                                for (Method method : bcelClass.getMethods()) {
                                    try {
                                        String name = method.getName();
                                        if (!name.equals("<init>")) {
                                            MethodMirror thisMirror = Reflection.getDeclaredMethod(klass, name, Type.getMethodType(method.getSignature()));
                                            bcelMethods.put(thisMirror, method);
                                        }
                                    } catch (NoSuchMethodException e1) {
                                        throw new RuntimeException(e1);
                                    }
                                }
                            }
                            bytecode = bcelMethods.get(methodMirror).getCode().getCode();
                        }
                        
                        InstructionList insnList = new InstructionList(bytecode);
                        for (InstructionHandle handle : insnList.getInstructionHandles()) {
                            short opcode = handle.getInstruction().getOpcode();
                            if ((kind == Shadow.SynchronizationLock && opcode == Opcodes.MONITORENTER)
                                || (kind == Shadow.SynchronizationUnlock && opcode == Opcodes.MONITOREXIT)) {
                                int offset = handle.getPosition();
                                installMonitorRequests(kind, methodMirror, offset);
                            }
                        }
                    }
                    
                } catch (ClassFormatException | IOException | SecurityException e) {
                    throw new RuntimeException(e);
                }
                
                return klass;
            };
        });
    }
    
    private void addFiltersAndInstall(MirrorEventRequest request, List<PatternNode> filters, Shadow.Kind kind, RequestContext context) {
        for (PatternNode pattern : filters) {
            addPatternFilter(request, pattern);
        }
        install(request, kind, context);
    }
    
    private void install(MirrorEventRequest request, Shadow.Kind kind, RequestContext context) {
        world.showMessage(IMessage.DEBUG, request.toString(), null, null);
        
        request.putProperty("for advice", advice);
        request.putProperty(SHADOW_KIND_PROPERTY_KEY, kind);
        world.vm.dispatch().addCallback(request, callback);

        if (context.cflowCounterGuard != null) {
            // If guarded by a !cflow(...) pointcut, register this request so that the
            // cflowEntry advice will enable and disable it as the cflow pointcut is entered and left.
            List<MirrorEventRequest> guardedRequests = cflowGuardedRequests.get(context.cflowCounterGuard);
            if (guardedRequests == null) {
                guardedRequests = new ArrayList<MirrorEventRequest>();
                cflowGuardedRequests.put(context.cflowCounterGuard, guardedRequests);
            }
            
            guardedRequests.add(request);
        } else {
            // Otherwise enable it unconditionally
            request.enable();
        }
    }
    
    private void installMethodRequests(final Shadow.Kind kind, final RequestContext context) {
        forAllWovenClasses(new Callback<ClassMirror>() {
            public ClassMirror handle(ClassMirror klass) {
                ReferenceType referenceType = (ReferenceType)world.resolve(klass);
                for (Member method : referenceType.getDeclaredMethods()) {
                    boolean matches = true;
                    for (PatternNode pattern : context.thisFilters) {
                        if (pattern instanceof SignaturePattern) {
                            if (!(((SignaturePattern)pattern).matches(method, world, false))) {
                                matches = false;
                                break;
                            }
                        } else if (pattern instanceof TypePattern) {
                            // TODO-RS: This would be more efficient outside the methods loop
                            if (!((TypePattern)pattern).matchesStatically(referenceType)) {
                                matches = false;
                                break;
                            }
                        } else {
//                            throw new IllegalStateException("Unsupported method event filter: " + pattern);
                        }
                    }
                    if (matches) {
                        MethodMirror methodMirror = ((MethodMirrorMember)method).method;
                        MethodMirrorHandlerRequest request = world.vm.eventRequestManager().createMethodMirrorHandlerRequest();
                        request.setMethodFilter(methodMirror.getDeclaringClass().getClassName(), methodMirror.getName(), methodMirror.getParameterTypeNames());
                        install(request, kind, context);
                    }
                }
                return klass;
            }
        });
    }
    
    private static final Map<ThreadMirror, Set<InstanceMirror>> ownedMonitors = 
            new HashMap<ThreadMirror, Set<InstanceMirror>>();
    
    private void installMonitorRequests(final Shadow.Kind kind, MethodMirror methodMirror, int offset) {
        MirrorLocation location = null;
        try {
            location = methodMirror.locationForBytecodeOffset(offset);
        } catch (UnsupportedOperationException e) {
            // TODO-RS: Working around lack of implementation in holographic classes
            // (i.e. BytecodeMethodMirror). The proper fix is to add synchronization
            // events/requests to the mirrors API and handle this differently in
            // VirtualMachineHolographs
            return;
        }
        
        MirrorLocationRequest request = manager.createLocationRequest(location);
        world.vm.dispatch().addCallback(request, new Callback<MirrorEvent>() {
            public MirrorEvent handle(MirrorEvent event) {
                MirrorLocationEvent locationEvent = (MirrorLocationEvent)event;
                ThreadMirror thread = locationEvent.thread();
                ownedMonitors.put(thread, new HashSet<InstanceMirror>(thread.getOwnedMonitors()));
                return event;
            } 
        });
        request.enable();
        
        location = methodMirror.locationForBytecodeOffset(offset + 1);
        request = manager.createLocationRequest(location);
        world.vm.dispatch().addCallback(request, new Callback<MirrorEvent>() {
            public MirrorEvent handle(MirrorEvent event) {
                MirrorLocationEvent locationEvent = (MirrorLocationEvent)event;
                ThreadMirror thread = locationEvent.thread();
                Set<InstanceMirror> monitorsBefore = ownedMonitors.get(thread);
                Set<InstanceMirror> monitorsAfter = new HashSet<InstanceMirror>(thread.getOwnedMonitors());
                Set<InstanceMirror> difference;
                if (kind == Shadow.SynchronizationLock) {
                    difference = monitorsAfter;
                    difference.removeAll(monitorsBefore);
                    
                } else {
                    difference = monitorsBefore;
                    difference.removeAll(monitorsAfter);
                }
                if (difference.isEmpty()) {
                    return event;
                }
                
                if (difference.size() != 1) {
                    throw new IllegalStateException();
                }
                InstanceMirror monitor = difference.iterator().next();
                MirrorEventShadow shadow;
                if (kind == Shadow.SynchronizationLock) {
                    shadow = new MirrorMonitorEnterShadow(world, locationEvent, monitor, !advice.getKind().isAfter(), null);
                } else {
                    shadow = new MirrorMonitorExitShadow(world, locationEvent, monitor, !advice.getKind().isAfter(), null);
                }
                world.handle(shadow);
                return event;
            }
        });
        request.enable();
    }
    
    private void addPatternFilter(MirrorEventRequest request, PatternNode pattern) {
        if (pattern instanceof TypePattern) {
            // TODO-RS: toString() is likely wrong here. Need to decide on 
            // what pattern DSL the mirrors API should accept.
            request.addClassFilter(pattern.toString());
        }
        // Ignore any other patterns
    }
    
    public static void installCallback(MirrorWorld world, Advice advice) {
        PointcutMirrorRequestExtractor extractor = new PointcutMirrorRequestExtractor(world, advice);
        Pointcut dnf = new PointcutRewriter().rewrite(advice.getPointcut());
        extractor.visit(dnf, null, new RequestContext());
    }
    
    
}
