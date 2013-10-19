package edu.ubc.retrospect;

import java.util.Map;

import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

public class MirrorTypeMunger extends ConcreteTypeMunger {

    public MirrorTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType) {
        super(munger, aspectType);
    }

    public void munge(MirrorWeaver weaver) {
    }
    
    @Override
    public ConcreteTypeMunger parameterizedFor(ResolvedType targetType) {
        return new MirrorTypeMunger(munger.parameterizedFor(targetType), aspectType);
    }

    @Override
    public ConcreteTypeMunger parameterizeWith(Map<String, UnresolvedType> parameterizationMap, World world) {
        return new MirrorTypeMunger(munger.parameterizeWith(parameterizationMap, world), aspectType);
    }

}
