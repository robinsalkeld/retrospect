package edu.ubc.retrospect;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;

public class MirrorCflowFieldAdder extends MirrorTypeMunger {

    private ResolvedMember cflowField;
    
    public MirrorCflowFieldAdder(ResolvedMember cflowField) {
        super(null, (ResolvedType)cflowField.getDeclaringType());
        this.cflowField = cflowField;
    }

    @Override
    public void munge(MirrorWeaver weaver) {
        ClassMirror klass = weaver.getWorld().mirrorForType(aspectType);
        ClassMirror fieldType = weaver.getWorld().mirrorForType(weaver.getWorld().resolve(cflowField.getType()));
        FieldMirror field = klass.createField(cflowField.getModifiers(), fieldType, cflowField.getName());
        
        try {
            InstanceMirror value = fieldType.getConstructor().newInstance(weaver.getWorld().thread);
            klass.getStaticFieldValues().set(field, value);
        } catch (IllegalAccessException | IllegalArgumentException
                | SecurityException | NoSuchMethodException
                | MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public ResolvedMember getMatchingSyntheticMember(Member member) {
            return null;
    }

    @Override
    public ResolvedMember getSignature() {
            return cflowField;
    }

    @Override
    public boolean matches(ResolvedType onType) {
            return onType.equals(cflowField.getDeclaringType());
    }

    @Override
    public boolean existsToSupportShadowMunging() {
            return true;
    }
}
