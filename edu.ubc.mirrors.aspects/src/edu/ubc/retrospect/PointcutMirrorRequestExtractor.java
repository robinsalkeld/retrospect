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
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.WithinPointcut;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;

/**
 * A visitor that installs the most specific possible event requests
 * that over-approximates all events that might match a given pointcut.
 * That is, it will install requests such that a given callback will be
 * invoked for a superset of the MirrorEvents that match a given pointcut.
 * 
 * @author robinsalkeld
 *
 */
public class PointcutMirrorRequestExtractor extends AbstractPatternNodeVisitor {

    private KindedPointcut kind;
    final List<PatternNode> filters = new ArrayList<PatternNode>();
    
    private final AdviceKind adviceKind;
    private final MirrorWorld world;
    private final Callback<MirrorEvent> callback;
    
    public PointcutMirrorRequestExtractor(MirrorWorld world, AdviceKind adviceKind, Callback<MirrorEvent> callback) {
        this.adviceKind = adviceKind;
        this.world = world;
        this.callback = callback;
    }

    @Override
    public Object visit(WithinPointcut node, Object belowAnd) {
        filters.add(node.getTypePattern());
        installIfNotBelowAnd(belowAnd);
        return null;
    }
    
    @Override
    public Object visit(KindedPointcut node, Object parent) {
        if (kind != null) {
            throw new IllegalArgumentException("More than one kinded pointcut found in clause: " + kind + ", " + node);
        }
        kind = node;
        installIfNotBelowAnd(parent);
        return null;
    }
    
    @Override
    public Object visit(OrPointcut node, Object parent) {
        node.getLeft().accept(this, node);
        
        kind = null;
        filters.clear();
        
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
            if (kind == null) {
                throw new IllegalArgumentException("No kinded pointcut found in: " + parent);
            }
            
            final MirrorEventRequestManager manager = world.vm.eventRequestManager();
            List<MirrorEventRequest> requests = new ArrayList<MirrorEventRequest>();
            
            switch (kind.getKind().bit) {
            case (Shadow.MethodExecutionBit):
                if (!adviceKind.isAfter() || adviceKind.isCflow()) {
                    requests.add(manager.createMethodMirrorEntryRequest());
                }
                if (adviceKind.isAfter() || adviceKind.isCflow()) {
                    requests.add(manager.createMethodMirrorExitRequest());
                }
                break;
            case (Shadow.ConstructorExecutionBit):
                if (!adviceKind.isAfter() || adviceKind.isCflow()) {
                    requests.add(manager.createConstructorMirrorEntryRequest());
                }
                if (adviceKind.isAfter() || adviceKind.isCflow()) {
                    requests.add(manager.createConstructorMirrorExitRequest());
                }
                break;
            case (Shadow.FieldSetBit):
                // TODO-RS: Optimize for the exact field match case
                // Copy the state to make sure the class prepare callback
                // reads the right state
                final KindedPointcut callbackKind = kind;
                final List<PatternNode> callbackFilters = new ArrayList<PatternNode>(filters);
                world.vm.dispatch().forAllClasses(new Callback<ClassMirror>() {
                    public void handle(ClassMirror klass) {
                        ReferenceType type = (ReferenceType)world.resolve(klass);
                        for (ResolvedMember field : type.getDeclaredFields()) {
                            if (callbackKind.getSignature().matches(field, world, false)) {
                                FieldMirrorMember member = (FieldMirrorMember)field;
                                MirrorEventRequest request = manager.createFieldMirrorSetRequest(member.getField());
                                addFiltersAndInstall(request, callbackKind, callbackFilters);
                            }
                        }
                    };
                });
                return;
            default: 
                throw new UnsupportedOperationException("Unsupported pointcut kind: " + kind.getKind());
            }
            
            // TODO-RS: Use forAllClasses as above to handle classes that are
            // defined later.
            for (MirrorEventRequest request : requests) {
                addFiltersAndInstall(request, kind, filters);
            }
        }
    }
    
    private void addFiltersAndInstall(MirrorEventRequest request, KindedPointcut kind, List<PatternNode> filters) {
        addPatternFilter(request, kind.getSignature());
        for (PatternNode pattern : filters) {
            addPatternFilter(request, pattern);
        }
        world.vm.dispatch().addCallback(request, callback);
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
    
    public static void installCallback(MirrorWorld world, AdviceKind adviceKind, Pointcut pc, Callback<MirrorEvent> callback) {
        PointcutMirrorRequestExtractor extractor = new PointcutMirrorRequestExtractor(world, adviceKind, callback);
        pc.accept(extractor, null);
    }
}
