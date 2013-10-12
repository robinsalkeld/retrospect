package edu.ubc.retrospect;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ast.Var;

public class MirrorEventVar extends Var {

    private final Object value;
    
    public MirrorEventVar(ResolvedType variableType, Object value) {
        super(variableType);
        this.value = value;
    }
    
    public Object getValue() {
        return value;
    }

}
