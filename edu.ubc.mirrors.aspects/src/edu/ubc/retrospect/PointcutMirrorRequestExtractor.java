package edu.ubc.retrospect;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AdviceKind;
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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationRequest;
import edu.ubc.mirrors.Reflection;
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
        public void handle(MirrorEvent event) {
            MirrorEventShadow shadow = MirrorEventShadow.make(world, event);
            callback.handle(shadow);
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
            for (final Shadow.Kind kind : Shadow.toSet(kinds)) {
                switch (kind.bit) {
                case (Shadow.MethodExecutionBit):
                    if (!advice.getKind().isAfter() || advice.getKind().isCflow()) {
                        addFiltersAndInstall(manager.createMethodMirrorEntryRequest(), thisFilters);
                    }
                    if (advice.getKind().isAfter() || advice.getKind().isCflow()) {
                        addFiltersAndInstall(manager.createMethodMirrorExitRequest(), thisFilters);
                    }
                    break;
                case (Shadow.ConstructorExecutionBit):
                    if (!advice.getKind().isAfter() || advice.getKind().isCflow()) {
                        addFiltersAndInstall(manager.createConstructorMirrorEntryRequest(), thisFilters);
                    }
                    if (advice.getKind().isAfter() || advice.getKind().isCflow()) {
                        addFiltersAndInstall(manager.createConstructorMirrorExitRequest(), thisFilters);
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
            public void handle(ClassMirror klass) {
                if (klass.getLoader() == null) {
                    return;
                }
                
                 // TODO-RS: Cheating to account for lack of requests/events on holographic execution
                if (klass instanceof ClassHolograph && ((ClassHolograph)klass).getWrapped() instanceof BytecodeClassMirror) {
                    return;
                }
                    
                callback.handle(klass);
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
            public void handle(ClassMirror klass) {
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
            public void handle(ClassMirror klass) {
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
                                mmer.setMethodFilter(methodMirror);
                                request = mmer;
                            } else if (advice.getKind().isAfter() && kind == Shadow.SynchronizationUnlock) {
                                MethodMirrorExitRequest mmer = manager.createMethodMirrorExitRequest();
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
                ClassReader reader = new ClassReader(klass.getBytecode());
                reader.accept(new SynchronizationClassVisitor(klass, kind), 0);
            };
        });
    }
    
    private void addFiltersAndInstall(MirrorEventRequest request, List<PatternNode> filters) {
        for (PatternNode pattern : filters) {
            addPatternFilter(request, pattern);
        }
        world.vm.dispatch().addCallback(request, EVENT_CALLBACK);
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
    
    private class SynchronizationClassVisitor extends ClassVisitor {

        private final ClassMirror klass;
        private final Shadow.Kind kind;
        
        public SynchronizationClassVisitor(ClassMirror klass, Shadow.Kind kind) {
            super(Opcodes.ASM4);
            this.klass = klass;
            this.kind = kind;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.startsWith("<")) {
                return null;
            }
            
            try {
                MethodMirror method = Reflection.getDeclaredMethod(klass, name, Type.getType(desc));
                return new SynchonizationMethodVisitor(access, name, desc, signature, exceptions, method, kind);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private class SynchonizationMethodVisitor extends MethodNode {

        private final MethodMirror methodMirror;
        private final Shadow.Kind kind;
        
        public SynchonizationMethodVisitor(int access, String name, String desc, String signature, String[] exceptions, MethodMirror methodMirror, Shadow.Kind kind) {
            super(access, name, desc, signature, exceptions);
            this.methodMirror = methodMirror;
            this.kind = kind;
        }

        @Override
        public void visitInsn(int opcode) {
            int offset = -1;
            if ((kind == Shadow.SynchronizationLock && opcode == Opcodes.MONITORENTER)
                    || kind == Shadow.SynchronizationUnlock && opcode == Opcodes.MONITOREXIT) {
                offset = instructions.size();
                if (advice.getKind().isAfter()) {
                    offset++;
                }
                
                MirrorLocation location = methodMirror.locationForBytecodeOffset(offset);
                MirrorLocationRequest request = manager.createLocationRequest(location);
                request.putProperty(MirrorEventShadow.SHADOW_KIND_PROPERTY_KEY, kind);
                request.putProperty(MirrorEventShadow.IS_ENTRY_PROPERTY_KEY, !advice.getKind().isAfter());
                world.vm.dispatch().addCallback(request, EVENT_CALLBACK);
                request.enable();
            }
        }
    }
}
