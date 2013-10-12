package edu.ubc.retrospect;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.ThreadMirror;

public abstract class MirrorEventShadow extends Shadow {

    protected MirrorEventShadow(MirrorWorld world, MirrorEvent event, Shadow.Kind kind, Member signature, Shadow enclosingShadow) {
        super(kind, signature, enclosingShadow);
        this.world = world;
        this.event = event;
    }

    protected final MirrorWorld world;
    private final MirrorEvent event;
    
    
    @Override
    public World getIWorld() {
        return world;
    }

    @Override
    public ISourceLocation getSourceLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract ThreadMirror getThread();
    
    protected Object getThis() {
        return getThread().getStackTrace().get(0).thisObject();
    }
    
    protected Object getArgument(int i) {
        return getThread().getStackTrace().get(0).arguments().get(i);
    }
    
    protected ResolvedType getJoinPointStaticPartType() {
        return world.resolve(UnresolvedType.forName(JoinPoint.StaticPart.class.getName()));
    }
    
    protected ResolvedType getJoinPointType() {
        return world.resolve(UnresolvedType.forName(JoinPoint.class.getName()));
    }
    
    public static MirrorEventShadow make(MirrorWorld world, MirrorEvent event) {
        if (event instanceof MethodMirrorEntryEvent) {
            MethodMirrorEntryEvent mmee = (MethodMirrorEntryEvent)event;
            Member signature = MethodMirrorMember.make(world, mmee.method());
            return new MethodMirrorEntryShadow(world, mmee, signature, null);
        } else {
            throw new IllegalArgumentException();
        }
    }
    

    @Override
    public Var getThisVar() {
        return null;
    }

    @Override
    public Var getTargetVar() {
        return null;
    }

    @Override
    public UnresolvedType getEnclosingType() {
        return null;
    }

    @Override
    public Var getArgVar(int i) {
        return null;
    }

    @Override
    public Var getThisJoinPointVar() {
        return null;
    }

    @Override
    public Var getThisJoinPointStaticPartVar() {
        return null;
    }

    @Override
    public Var getThisEnclosingJoinPointStaticPartVar() {
        return null;
    }

    @Override
    public Var getKindedAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getWithinAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getWithinCodeAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getThisAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getTargetAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getArgAnnotationVar(int i, UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Member getEnclosingCodeSignature() {
        return null;
    }
    
}
