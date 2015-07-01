package edu.ubc.retrospect;

import java.util.List;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.ConstructorMirrorHandlerEvent;
import edu.ubc.mirrors.FieldMirrorGetEvent;
import edu.ubc.mirrors.FieldMirrorGetHandlerEvent;
import edu.ubc.mirrors.FieldMirrorSetEvent;
import edu.ubc.mirrors.FieldMirrorSetHandlerEvent;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MethodMirrorHandlerEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;

public abstract class MirrorEventShadow extends Shadow {

    protected final MirrorWorld world;
    protected final MirrorEvent event;
    
    protected MirrorEventShadow(MirrorWorld world, MirrorEvent event, Shadow.Kind kind, Member signature, Shadow enclosingShadow) {
        super(kind, signature, enclosingShadow);
        this.world = world;
        this.event = event;
    }

    /**
     * This equality is defined in terms of shadows, not instantaneous events.
     * That is, two events that arise from the same place in the code, but perhaps at
     * different times with different dynamic properties, should still compare equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        MirrorEventShadow other = (MirrorEventShadow)obj;
        return getKind().equals(other.getKind()) && equals(other);
    }
    
    protected abstract boolean equals(MirrorEventShadow other);
    
    @Override
    public abstract int hashCode();
    
    @Override
    public World getIWorld() {
        return world;
    }

    public MirrorEvaluator getEvaluator(List<Object> arguments) {
        return new MirrorEvaluator(world, getThread(), arguments);
    }
    
    @Override
    public ResolvedType getEnclosingType() {
        return world.resolve(getDeclaringClass());
    }
    
    protected abstract ClassMirror getDeclaringClass();
    
    public abstract AdviceKind adviceKind();
    
    @Override
    protected void prepareForMungers() {
//        if (kind() == AdviceKind.Before) {
//            Collections.reverse(mungers);
//        }
    }
    
    @Override
    public ISourceLocation getSourceLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract ThreadMirror getThread();
    
    protected InstanceMirror getThis() {
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
    
    public static MirrorEventShadow make(MirrorWorld world, MirrorEvent event, Shadow.Kind shadowKind) {
        if (event instanceof ConstructorMirrorHandlerEvent) {
            ConstructorMirrorHandlerEvent cmhe = (ConstructorMirrorHandlerEvent)event;
            if (shadowKind == Shadow.ConstructorCall && cmhe.isConstructorChaining()) {
                return null;
            }
            
            Member signature = ConstructorMirrorMember.make(world, cmhe.constructor());
            return new ConstructorMirrorExecutionShadow(world, shadowKind, AdviceKind.Around, cmhe, cmhe.constructor(),
                    cmhe.thread(), signature, null);
        } else if (event instanceof ConstructorMirrorEntryEvent) {
            ConstructorMirrorEntryEvent cmee = (ConstructorMirrorEntryEvent)event;
            if (shadowKind == Shadow.ConstructorCall && cmee.isConstructorChaining()) {
                return null;
            }
            
            Member signature = ConstructorMirrorMember.make(world, cmee.constructor());
            return new ConstructorMirrorExecutionShadow(world, shadowKind, AdviceKind.Before, cmee, cmee.constructor(),
                    cmee.thread(), signature, null);
        } else if (event instanceof ConstructorMirrorExitEvent) {
            ConstructorMirrorExitEvent cmee = (ConstructorMirrorExitEvent)event;
            Member signature = ConstructorMirrorMember.make(world, cmee.constructor());
            return new ConstructorMirrorExecutionShadow(world, shadowKind, AdviceKind.After, cmee, cmee.constructor(),
                    cmee.thread(), signature, null);
        } else if (event instanceof MethodMirrorHandlerEvent) {
            MethodMirrorHandlerEvent mmhe = (MethodMirrorHandlerEvent)event;
            if (isNonwovenMethod(mmhe.method())) {
                return null;
            }
            
            Member signature = MethodMirrorMember.make(world, mmhe.method());
            return new MethodMirrorExecutionShadow(world, shadowKind, mmhe, signature, null);
        } else if (event instanceof MethodMirrorEntryEvent) {
            MethodMirrorEntryEvent mmee = (MethodMirrorEntryEvent)event;
            if (isNonwovenMethod(mmee.method())) {
                return null;
            }
            
            if (shadowKind == Shadow.SynchronizationLock) {
                return new SynchronizedMethodMirrorEntryShadow(world, mmee, null);
            } else {
                Member signature = MethodMirrorMember.make(world, mmee.method());
                return new MethodMirrorEntryShadow(world, shadowKind, mmee, signature, null);
            }
        } else if (event instanceof MethodMirrorExitEvent) {
            MethodMirrorExitEvent mmee = (MethodMirrorExitEvent)event;
            if (isNonwovenMethod(mmee.method())) {
                return null;
            }
            
            if (shadowKind == Shadow.SynchronizationUnlock) {
                return new SynchronizedMethodMirrorExitShadow(world, mmee, null);
            } else {
                Member signature = MethodMirrorMember.make(world, mmee.method());
                return new MethodMirrorExitShadow(world, shadowKind, mmee, signature, null);
            }
        } else if (event instanceof FieldMirrorGetEvent) {
            FieldMirrorGetEvent fmge = (FieldMirrorGetEvent)event;
            Member signature = FieldMirrorMember.make(world, fmge.field());
            return new FieldMirrorGetShadow(world, fmge, signature, null);
        } else if (event instanceof FieldMirrorSetEvent) {
            FieldMirrorSetEvent fmge = (FieldMirrorSetEvent)event;
            Member signature = FieldMirrorMember.make(world, fmge.field());
            return new FieldMirrorSetShadow(world, fmge, signature, null);
        } else if (event instanceof FieldMirrorGetHandlerEvent) {
            FieldMirrorGetHandlerEvent fmghe = (FieldMirrorGetHandlerEvent)event;
            Member signature = FieldMirrorMember.make(world, fmghe.field());
            return new FieldMirrorGetHandlerShadow(world, fmghe, signature, null);
        } else if (event instanceof FieldMirrorSetHandlerEvent) {
            FieldMirrorSetHandlerEvent fmshe = (FieldMirrorSetHandlerEvent)event;
            Member signature = FieldMirrorMember.make(world, fmshe.field());
            return new FieldMirrorSetHandlerShadow(world, fmshe, signature, null);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    private static boolean isNonwovenMethod(MethodMirror method) {
        String methodName = method.getName();
        
        if (methodName.equals("ajc$preClinit") 
                || methodName.equals("ajc$postClinit")
                || methodName.equals("<clinit>")) {
            return true;
        } else {
            return false;
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
    public Var getArgVar(int i) {
        return null;
    }

    @Override
    public Var getThisJoinPointVar() {
        ResolvedType joinPointType = world.resolve(UnresolvedType.forName(JoinPoint.class.getName()));
        return new MirrorEventVar(joinPointType, null);
    }

    @Override
    public Var getThisJoinPointStaticPartVar() {
        ResolvedType joinPointStaticPartType = world.resolve(UnresolvedType.forName(JoinPoint.StaticPart.class.getName()));
        return new MirrorEventVar(joinPointStaticPartType, getThisJoinPointStaticPart());
    }

    protected abstract InstanceMirror getThisJoinPointStaticPart();

    @Override
    public Var getThisEnclosingJoinPointStaticPartVar() {
        ResolvedType joinPointStaticPartType = world.resolve(UnresolvedType.forName(JoinPoint.StaticPart.class.getName()));
        return new MirrorEventVar(joinPointStaticPartType, getThisEnclosingJoinPointStaticPart());
    }

    protected InstanceMirror getThisEnclosingJoinPointStaticPart() {
        return staticPartForFrame(getThread().getStackTrace().get(1));
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
    public Var getThisAspectInstanceVar(ResolvedType aspectType) {
        return null;
    }
    
    protected InstanceMirror staticPartForFrame(FrameMirror frame) {
        ConstructorMirror cons = frame.constructor();
        if (cons != null) {
            return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.CONSTRUCTOR_EXECUTION, cons);
        } else {
            return world.makeStaticJoinPoint(getThread(), org.aspectj.lang.JoinPoint.METHOD_EXECUTION, frame.method());
        }
    }
    
    public void implementAdvice(final MirrorAdvice advice) {
        final MirrorInvocationHandler handler = event.getProceed();
        MirrorInvocationHandler newHandler = new MirrorInvocationHandler() {
            @Override
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                return advice.testAndExecute(MirrorEventShadow.this, handler, args);
            }
        };
        event.setProceed(newHandler);
    }
    
//    @Override
//    public String toString() {
//        return kind() + " " + super.toString();
//    }
}
