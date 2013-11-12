package edu.ubc.retrospect;

import java.util.ArrayList;
import java.util.List;

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
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.WithinPointcut;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.jdi.JDIFieldMirror;
import edu.ubc.mirrors.wrapping.WrappingFieldMirror;

/**
 * A visitor that installs the most specific possible event requests
 * that over-approximates all events that might match a given pointcut.
 * That is, it will install requests such that a given callback will be
 * invoked for a superset of the MirrorEvents that match a given pointcut.
 * 
 * @author Robin Salkeld
 *
 */
public class PointcutMirrorRequestExtractor extends AbstractPatternNodeVisitor {

    int kinds = Shadow.ALL_SHADOW_KINDS_BITS;
    final List<PatternNode> thisFilters = new ArrayList<PatternNode>();
    final List<PatternNode> targetFilters = new ArrayList<PatternNode>();
     
    private final AdviceKind adviceKind;
    private final MirrorWorld world;
    private final Callback<MirrorEventShadow> callback;
    
    private final Callback<MirrorEvent> EVENT_CALLBACK = new Callback<MirrorEvent>() {
        @Override
        public void handle(MirrorEvent event) {
            MirrorEventShadow shadow = MirrorEventShadow.make(world, event);
            callback.handle(shadow);
        }
    };
    
    public PointcutMirrorRequestExtractor(MirrorWorld world, AdviceKind adviceKind, Callback<MirrorEventShadow> callback) {
        this.adviceKind = adviceKind;
        this.world = world;
        this.callback = callback;
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
    
    public void installIfNotBelowAnd(Object parent) {
        if (!(parent instanceof AndPointcut)) {
            final MirrorEventRequestManager manager = world.vm.eventRequestManager();
            
            for (final Shadow.Kind kind : Shadow.toSet(kinds)) {
                switch (kind.bit) {
                case (Shadow.MethodExecutionBit):
                    if (!adviceKind.isAfter() || adviceKind.isCflow()) {
                        addFiltersAndInstall(manager.createMethodMirrorEntryRequest(), thisFilters);
                    }
                    if (adviceKind.isAfter() || adviceKind.isCflow()) {
                        addFiltersAndInstall(manager.createMethodMirrorExitRequest(), thisFilters);
                    }
                    break;
                case (Shadow.ConstructorExecutionBit):
                    if (!adviceKind.isAfter() || adviceKind.isCflow()) {
                        addFiltersAndInstall(manager.createConstructorMirrorEntryRequest(), thisFilters);
                    }
                    if (adviceKind.isAfter() || adviceKind.isCflow()) {
                        addFiltersAndInstall(manager.createConstructorMirrorExitRequest(), thisFilters);
                    }
                    break;
                case (Shadow.FieldSetBit):
                case (Shadow.FieldGetBit):
                        // TODO-RS: Optimize for the exact field match case
                    // Copy the state to make sure the class prepare callback
                    // reads the right state.
                    final List<PatternNode> callbackFilters = new ArrayList<PatternNode>(thisFilters);
                    world.vm.dispatch().forAllClasses(new Callback<ClassMirror>() {
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
                                    // TODO-RS: Cheating to account for lack of requests/events on holographic execution
                                    if (fieldMirror instanceof WrappingFieldMirror && ((WrappingFieldMirror)fieldMirror).getWrapped() instanceof JDIFieldMirror) {
                                        if (kind.bit == Shadow.FieldSetBit) {
                                            request = manager.createFieldMirrorSetRequest(fieldMirror);
                                        } else {
                                            request = manager.createFieldMirrorGetRequest(fieldMirror);
                                        }
                                        addFiltersAndInstall(request, callbackFilters);
                                    }
                                }
                            }
                        };
                    });
                    return;
                case Shadow.ConstructorCallBit:
                case Shadow.MethodCallBit:
                case Shadow.AdviceExecutionBit:
                case Shadow.InitializationBit:
                case Shadow.PreInitializationBit:
                case Shadow.ExceptionHandlerBit:
                case Shadow.SynchronizationLockBit:
                case Shadow.SynchronizationUnlockBit:
                    // TODO-RS
                    return;
                }
            }
        }
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
    
    public static void installCallback(MirrorWorld world, AdviceKind adviceKind, Pointcut pc, Callback<MirrorEventShadow> callback) {
        PointcutMirrorRequestExtractor extractor = new PointcutMirrorRequestExtractor(world, adviceKind, callback);
        pc.accept(extractor, null);
    }
}
