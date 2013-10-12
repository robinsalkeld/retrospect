package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.patterns.AbstractPatternNodeVisitor;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.WithinPointcut;

import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.VirtualMachineMirror;

/**
 * A two-pass visitor that creates the most specific possible event request
 * that covers all events that might match a given pointcut.
 * @author robinsalkeld
 *
 */
public class PointcutMirrorRequestExtractor extends AbstractPatternNodeVisitor {

    private final AdviceKind adviceKind;
    private final VirtualMachineMirror vm;
    private MirrorEventRequest request;
    private boolean firstPass = true;
    
    public PointcutMirrorRequestExtractor(VirtualMachineMirror vm, AdviceKind adviceKind) {
        this.adviceKind = adviceKind;
        this.vm = vm;
    }

    private void addTypePatternFilter(TypePattern pattern) {
     // TODO-RS: toString() is likely wrong here. Need to decide on 
        // what pattern DSL the mirrors API should accept.
        request.addClassFilter(pattern.toString());
    }
    
    @Override
    public Object visit(WithinPointcut node, Object data) {
        if (!firstPass) {
            // TODO-RS: Should remove clauses that are completely expressed
            // as request filters, to avoid redundant checking.
            addTypePatternFilter(node.getTypePattern());
        }
        
        return data;
    }
    
    @Override
    public Object visit(KindedPointcut node, Object data) {
        if (firstPass) {
            if (request != null) {
                throw new IllegalStateException("Conflicting pointcut kinds");
            }
            
            MirrorEventRequestManager manager = vm.eventRequestManager();
            switch (node.getKind().bit) {
            case (Shadow.MethodExecutionBit):
                if (adviceKind.isAfter()) {
                    request = manager.createMethodMirrorExitRequest();
                } else {
                    request = manager.createMethodMirrorEntryRequest();
                }
                break;
            case (Shadow.ConstructorExecutionBit):
                if (adviceKind.isAfter()) {
                    request = manager.createConstructorMirrorExitRequest();
                } else {
                    request = manager.createConstructorMirrorEntryRequest();
                }
                break;
            default: 
                throw new UnsupportedOperationException("Unsupported pointcut kind: " + node.getKind());
            }
        } else {
            // TODO-RS: Should remove clauses that are completely expressed
            // as request filters, to avoid redundant checking.
            addTypePatternFilter(node.getSignature().getDeclaringType());
            
        }
        
        return data;
    }
    
    
    public static MirrorEventRequest extractRequest(VirtualMachineMirror vm, AdviceKind adviceKind, Pointcut pc) {
        PointcutMirrorRequestExtractor extractor = new PointcutMirrorRequestExtractor(vm, adviceKind);
        pc.traverse(extractor, null);
        
        if (extractor.request == null) {
            throw new IllegalArgumentException("No kinded pointcut found: " + pc);
        }
        
        extractor.firstPass = false;
        pc.traverse(extractor, null);
        
        return extractor.request;
    }
}
