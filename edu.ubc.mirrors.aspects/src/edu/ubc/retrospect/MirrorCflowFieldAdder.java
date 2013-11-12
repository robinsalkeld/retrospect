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
        // TODO-RS: Need to check for the field already existing because of compile-time weaving.
        // Ideally the source aspect should probably just be compiled and not woven against itself.
        FieldMirror field = klass.getDeclaredField(cflowField.getName());
        if (field == null) {
            field = klass.createField(cflowField.getModifiers(), fieldType, cflowField.getName());
        }
        
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
