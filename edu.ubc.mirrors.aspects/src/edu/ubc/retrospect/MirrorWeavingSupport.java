package edu.ubc.retrospect;

import org.aspectj.weaver.Advice;
import org.aspectj.weaver.AjAttribute.AdviceAttribute;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.IWeavingSupport;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.TemporaryTypeMunger;
import org.aspectj.weaver.ast.Var;
import org.aspectj.weaver.patterns.PerClause.Kind;
import org.aspectj.weaver.patterns.Pointcut;

public class MirrorWeavingSupport implements IWeavingSupport {

    private final MirrorWorld world;
    
    public MirrorWeavingSupport(MirrorWorld world) {
        this.world = world;
    }

    @Override
    public Advice createAdviceMunger(AdviceAttribute attribute, Pointcut pointcut, Member signature, ResolvedType concreteAspect) {
        return new AdviceMirror(world, attribute, concreteAspect, signature, pointcut);
    }

    @Override
    public ConcreteTypeMunger makeCflowStackFieldAdder(ResolvedMember cflowField) {
        return new MirrorCflowFieldAdder(cflowField);
    }

    @Override
    public ConcreteTypeMunger makeCflowCounterFieldAdder(ResolvedMember cflowField) {
        return new MirrorCflowFieldAdder(cflowField);
    }

    @Override
    public ConcreteTypeMunger makePerClauseAspect(ResolvedType aspect, Kind kind) {
        return new MirrorTypeMunger(null, aspect);
    }

    @Override
    public ConcreteTypeMunger concreteTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType) {
        return new MirrorTypeMunger(munger, aspectType);
    }

    @Override
    public ConcreteTypeMunger createAccessForInlineMunger(ResolvedType inAspect) {
        return new MirrorTypeMunger(null, inAspect);
    }

    @Override
    public Var makeCflowAccessVar(ResolvedType formalType, Member cflowField, int arrayIndex) {
        // TODO Auto-generated method stub
        return null;
    }

}
