package edu.ubc.retrospect;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.patterns.AbstractPatternNodeVisitor;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.Pointcut;

import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.VirtualMachineMirror;

public class PointcutMirrorRequestExtractor extends AbstractPatternNodeVisitor {

    private final AdviceKind adviceKind;
    private final VirtualMachineMirror vm;
    private MirrorEventRequest request;
    
    public PointcutMirrorRequestExtractor(VirtualMachineMirror vm, AdviceKind adviceKind) {
        this.adviceKind = adviceKind;
        this.vm = vm;
    }

    @Override
    public Object visit(KindedPointcut node, Object data) {
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
        
        // TODO-RS: Should remove clauses that are completely expressed
        // as request filters, to avoid redundant checking.
        
        // TODO-RS: toString() is likely wrong here. Need to decide on 
        // what pattern DSL the mirrors API should accept.
        request.addClassFilter(node.getSignature().getDeclaringType().toString());
        
        return data;
    }
    
    
    public static MirrorEventRequest extractRequest(VirtualMachineMirror vm, AdviceKind adviceKind, Pointcut pc) {
        PointcutMirrorRequestExtractor extractor = new PointcutMirrorRequestExtractor(vm, adviceKind);
        pc.traverse(extractor, null);
        return extractor.request;
    }
}
