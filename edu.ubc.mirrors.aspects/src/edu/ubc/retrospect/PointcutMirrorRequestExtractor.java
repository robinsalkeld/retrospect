package edu.ubc.retrospect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.aspectj.weaver.patterns.AbstractPatternNodeVisitor;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.PatternNode;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.PointcutRewriter;
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.WithinPointcut;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorHandlerRequest;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.InvocableMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationEvent;
import edu.ubc.mirrors.MirrorLocationRequest;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.raw.BytecodeClassMirror;

/**
 * A visitor that installs the most specific possible event requests
 * that over-approximates all events that might match a given pointcut.
 * That is, it will install requests such that a given callback will be
 * invoked for a superset of the Shadows that match a given pointcut.
 * 
 * @author Robin Salkeld
 *
 */
public class PointcutMirrorRequestExtractor extends AbstractPatternNodeVisitor {

    int kinds = Shadow.ALL_SHADOW_KINDS_BITS;
    final List<PatternNode> thisFilters = new ArrayList<PatternNode>();
    final List<PatternNode> targetFilters = new ArrayList<PatternNode>();
     
    private final Advice advice;
    private final MirrorWorld world;
    private final Callback<MirrorEventShadow> callback;
    private final MirrorEventRequestManager manager;
    
    private final Callback<MirrorEvent> EVENT_CALLBACK = new Callback<MirrorEvent>() {
        @Override
        public Object handle(MirrorEvent event) {
            MirrorEventShadow shadow = MirrorEventShadow.make(world, event);
            return callback.handle(shadow);
        }
    };
    
    private final MirrorInvocationHandler METHOD_INVOCATION_CALLBACK = new MirrorInvocationHandler() {
        public Object invoke(ThreadMirror thread, InvocableMirror invocable, List<Object> args, MirrorInvocationHandler original) throws MirrorInvocationTargetException {
            MethodMirror method = (MethodMirror)invocable;
            Member signature = MethodMirrorMember.make(world, method);
            MethodMirrorExecutionShadow shadow = new MethodMirrorExecutionShadow(world, method, thread, original, signature, null);
            return callback.handle(shadow);
        }
    };
    
    private final MirrorInvocationHandler CONSTRUCTOR_INVOCATION_CALLBACK = new MirrorInvocationHandler() {
        public Object invoke(ThreadMirror thread, InvocableMirror invocable, List<Object> args, MirrorInvocationHandler original) throws MirrorInvocationTargetException {
            ConstructorMirror constructor = (ConstructorMirror)invocable;
            Member signature = ConstructorMirrorMember.make(world, constructor);
            ConstructorMirrorExecutionShadow shadow = new ConstructorMirrorExecutionShadow(world, AdviceKind.Around, constructor, thread, original, signature, null);
            return callback.handle(shadow);
        }
    };
    
    public PointcutMirrorRequestExtractor(MirrorWorld world, Advice advice, Callback<MirrorEventShadow> callback) {
        this.advice = advice;
        this.world = world;
        this.callback = callback;
        this.manager = world.vm.eventRequestManager();
    }
    
    @Override
    public Object visit(WithinPointcut node, Object belowAnd) {
        kinds &= node.couldMatchKinds();
        thisFilters.add(node.getTypePattern());
        installIfNotBelowAnd(belowAnd);
        return null;
    }
    
    @Override
    public Object visit(KindedPointcut node, Object parent) {
        kinds &= node.couldMatchKinds();
        if (node.getKind().bit == Shadow.FieldSetBit) {
            targetFilters.add(node.getSignature());
        } else {
            thisFilters.add(node.getSignature());
        }
        installIfNotBelowAnd(parent);
        return null;
    }
    
    @Override
    public Object visit(OrPointcut node, Object parent) {
        node.getLeft().accept(this, node);
        
        kinds = Shadow.ALL_SHADOW_KINDS_BITS;
        thisFilters.clear();
        
        node.getRight().accept(this, node);

        return null;
    }
    
    @Override
    public Object visit(AndPointcut node, Object parent) {
        node.getLeft().accept(this, node);
        node.getRight().accept(this, node);
        installIfNotBelowAnd(parent);
        return null;
    }
    
    private void installIfNotBelowAnd(Object parent) {
        if (!(parent instanceof AndPointcut)) {
            AdviceKind adviceKind = advice.getKind();
            for (final Shadow.Kind kind : Shadow.toSet(kinds)) {
                switch (kind.bit) {
                case (Shadow.MethodExecutionBit):
                    if (adviceKind == AdviceKind.Before || adviceKind.isCflow()) {
                        addFiltersAndInstall(manager.createMethodMirrorEntryRequest(), thisFilters);
                    }
                    if (adviceKind.isAfter() || adviceKind.isCflow()) {
                        addFiltersAndInstall(manager.createMethodMirrorExitRequest(), thisFilters);
                    }
                    if (adviceKind == AdviceKind.Around) {
                        addFiltersAndInstall(manager.createMethodMirrorHandlerRequest(METHOD_INVOCATION_CALLBACK), thisFilters);
                    }
                    break;
                case (Shadow.ConstructorExecutionBit):
                    if (adviceKind == AdviceKind.Before || adviceKind.isCflow()) {
                        addFiltersAndInstall(manager.createConstructorMirrorEntryRequest(), thisFilters);
                    }
                    if (adviceKind.isAfter() || adviceKind.isCflow()) {
                        addFiltersAndInstall(manager.createConstructorMirrorExitRequest(), thisFilters);
                    }
                    if (adviceKind == AdviceKind.Around) {
                        addFiltersAndInstall(manager.createConstructorMirrorHandlerRequest(CONSTRUCTOR_INVOCATION_CALLBACK), thisFilters);
                    }
                    break;
                case (Shadow.FieldSetBit):
                case (Shadow.FieldGetBit):
                    installFieldRequests(kind);
                    break;
                case Shadow.SynchronizationLockBit:
                case Shadow.SynchronizationUnlockBit:
                    installSynchronizationRequests(kind);
                    break;
                case Shadow.ConstructorCallBit:
                case Shadow.MethodCallBit:
                case Shadow.AdviceExecutionBit:
                case Shadow.InitializationBit:
                case Shadow.PreInitializationBit:
                case Shadow.ExceptionHandlerBit:
                    // TODO-RS
//                    System.err.println("Unsupported pointcut kind: " + kind);
                    break;
                }
            }
        }
    }
    
    private void forAllWovenClasses(final Callback<ClassMirror> callback) {
        world.vm.dispatch().forAllClasses(new Callback<ClassMirror>() {
            public Object handle(ClassMirror klass) {
                if (klass.getLoader() == null) {
                    return null;
                }
                
                 // TODO-RS: Cheating to account for lack of requests/events on holographic execution
                if (klass instanceof ClassHolograph && ((ClassHolograph)klass).getWrapped() instanceof BytecodeClassMirror) {
                    return null;
                }
                    
                return callback.handle(klass);
            }
        });
    }
    
    private void installFieldRequests(final Shadow.Kind kind) {
        // No point in tracking cflow state in these, since get()/set() joinpoints
        // never contain any other joinpoints.
        if (advice.getKind().isCflow()) {
            return;
        }
        
        // TODO-RS: Optimize for the exact field match case
        // Copy the state to make sure the class prepare callback
        // reads the right state.
        final List<PatternNode> callbackFilters = new ArrayList<PatternNode>(thisFilters);
        forAllWovenClasses(new Callback<ClassMirror>() {
            public Object handle(ClassMirror klass) {
                ReferenceType type = (ReferenceType)world.resolve(klass);
                for (ResolvedMember field : type.getDeclaredFields()) {
                    boolean matches = true;
                    for (PatternNode targetPattern : targetFilters) {
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
                        if (kind.bit == Shadow.FieldSetBit) {
                            request = manager.createFieldMirrorSetRequest(fieldMirror);
                        } else {
                            request = manager.createFieldMirrorGetRequest(fieldMirror);
                        }
                        addFiltersAndInstall(request, callbackFilters);
                    }
                }
                
                return null;
            };
        });
    }
    
    private void installSynchronizationRequests(final Shadow.Kind kind) {
        // No point in tracking cflow state in these, since lock()/unlock() joinpoints
        // never contain any other joinpoints.
        if (advice.getKind().isCflow()) {
            return;
        }
        
        
        // TODO-RS: This isn't perfect, since I'm only supporting execution() shadows
        // at the moment, which implies that only after: lock() and before: unlock() work.
        // Realistically it doesn't matter unless the code queries the thread state in very specific ways.
        final List<PatternNode> callbackFilters = new ArrayList<PatternNode>(thisFilters);
        forAllWovenClasses(new Callback<ClassMirror>() {
            public Object handle(ClassMirror klass) {
                ReferenceType type = (ReferenceType)world.resolve(klass);
                
                // These are handled in two parts:
                // 1. Delegate to handling the pointcut call(synchronized * *.*(..)), to 
                //    catch all synchronized methods.
                for (ResolvedMember method : type.getDeclaredMethods()) {
                    if (Modifier.isSynchronized(method.getModifiers())) {
                        MethodMirrorMember member = (MethodMirrorMember)method;
                        MirrorEventRequest request;
                        MethodMirror methodMirror = (MethodMirror)member.getMethod();
                        // TODO-RS: Cheating to account for lack of requests/events on holographic execution
//                        if (methodMirror instanceof WrappingMethodMirror && !(((WrappingMethodMirror)methodMirror).getWrapped() instanceof JDIFieldMirror)) {
                            if (advice.getKind() == AdviceKind.Before && kind == Shadow.SynchronizationLock) {
                                MethodMirrorEntryRequest mmer = manager.createMethodMirrorEntryRequest();
                                mmer.putProperty(MirrorEventShadow.SHADOW_KIND_PROPERTY_KEY, kind);
                                mmer.setMethodFilter(methodMirror);
                                request = mmer;
                            } else if (advice.getKind().isAfter() && kind == Shadow.SynchronizationUnlock) {
                                MethodMirrorExitRequest mmer = manager.createMethodMirrorExitRequest();
                                mmer.putProperty(MirrorEventShadow.SHADOW_KIND_PROPERTY_KEY, kind);
                                mmer.setMethodFilter(methodMirror);
                                request = mmer;
                            } else {
                                throw new IllegalArgumentException("Unsupported lock()/unlock() advice kind: " + advice);
                            }
                            addFiltersAndInstall(request, callbackFilters);
//                        }
                    }
                }
                
                // 2. Search through the bytecode for MONITORENTER/EXIT instructions,
                //    and create breakpoints for each.
                try {
                    Map<MethodMirror, Method> bcelMethods = null;
                    for (MethodMirror methodMirror : klass.getDeclaredMethods(false)) {
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
                                        if (!name.startsWith("<")) {
                                            MethodMirror thisMirror = Reflection.getDeclaredMethod(klass, name, Type.getMethodType(method.getDeclaredSignature()));
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
                
                return null;
            };
        });
    }
    
    private void addFiltersAndInstall(MirrorEventRequest request, List<PatternNode> filters) {
        for (PatternNode pattern : filters) {
            addPatternFilter(request, pattern);
        }
        if (request instanceof MethodMirrorHandlerRequest || request instanceof ConstructorMirrorHandlerRequest) {
            request.enable();
        } else {
            world.showMessage(IMessage.DEBUG, request.toString(), null, null);
            world.vm.dispatch().addCallback(request, EVENT_CALLBACK);
        }
        request.enable();
    }
    
    private final Map<ThreadMirror, Set<InstanceMirror>> ownedMonitors = 
            new HashMap<ThreadMirror, Set<InstanceMirror>>();
    
    private void installMonitorRequests(final Shadow.Kind kind, MethodMirror methodMirror, int offset) {
        MirrorLocation location = methodMirror.locationForBytecodeOffset(offset);
        MirrorLocationRequest request = manager.createLocationRequest(location);
        world.vm.dispatch().addCallback(request, new Callback<MirrorEvent>() {
            public Object handle(MirrorEvent event) {
                MirrorLocationEvent locationEvent = (MirrorLocationEvent)event;
                ThreadMirror thread = locationEvent.thread();
                ownedMonitors.put(thread, new HashSet<InstanceMirror>(thread.getOwnedMonitors()));
                return null;
            } 
        });
        request.enable();
        
        location = methodMirror.locationForBytecodeOffset(offset + 1);
        request = manager.createLocationRequest(location);
        world.vm.dispatch().addCallback(request, new Callback<MirrorEvent>() {
            public Object handle(MirrorEvent event) {
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
                return callback.handle(shadow);
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
    
    public static void installCallback(MirrorWorld world, Advice advice, Callback<MirrorEventShadow> callback) {
        PointcutMirrorRequestExtractor extractor = new PointcutMirrorRequestExtractor(world, advice, callback);
        Pointcut dnf = new PointcutRewriter().rewrite(advice.getPointcut());
        dnf.accept(extractor, null);
    }
}
