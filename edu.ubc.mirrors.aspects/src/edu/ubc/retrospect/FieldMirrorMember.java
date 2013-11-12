package edu.ubc.retrospect;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberKind;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.UnresolvedType;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.MethodMirror;

public class FieldMirrorMember extends ResolvedMemberImpl {

    private final FieldMirror field;
    
    public FieldMirrorMember(FieldMirror field, UnresolvedType declaringType,
            int modifiers, UnresolvedType returnType, String name) {
        super(Member.FIELD, declaringType, modifiers, returnType, name, UnresolvedType.NONE);
        this.field = field;
    }

    public FieldMirror getField() {
        return field;
    }
    
    public static FieldMirrorMember make(MirrorWorld world, FieldMirror field) {
        UnresolvedType declaringType = world.resolve(field.getDeclaringClass());
        UnresolvedType returnType = UnresolvedType.forName(field.getTypeName());
        
        return new FieldMirrorMember(field, declaringType, field.getModifiers(), returnType, field.getName());
    }
    
}
