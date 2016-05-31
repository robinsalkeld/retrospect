/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.mirrors.raw;

import static edu.ubc.mirrors.holograms.HologramClassGenerator.classType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BoxingInstanceMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.StaticFieldValuesMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.fieldmap.FieldMapFieldMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;

// TODO-RS: This should probably be split up into multiple top-level classes in their
// own package at this point.
public abstract class BytecodeClassMirror extends BoxingInstanceMirror implements ClassMirror {

    public class BytecodeFieldMirror extends FieldMapFieldMirror {

        private final String desc;
        private final Object value;
        
        public BytecodeFieldMirror(BytecodeClassMirror klass, int access, String name, String desc, Object value) {
            super(klass, access, name, null);
            this.desc = desc;
            this.value = value;
        }

        @Override
        public String getTypeName() {
            return Type.getType(desc).getClassName();
        }
        
        @Override
        public ClassMirror getType() {
            if (type == null) {
                type = loadClassMirrorInternal(Type.getType(desc));
            }
            return type;
        }
    }

    @Override
    public Object getBoxedValue(FieldMirror field) throws IllegalAccessException {
        return null;
    }
    
    @Override
    public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }
    
    private final StaticFieldValuesMirror staticFieldValues = new BytecodeInstanceMirror();
    
    @Override
    public StaticFieldValuesMirror getStaticFieldValues() {
        return staticFieldValues;
    }
    
    private class BytecodeInstanceMirror extends BoxingInstanceMirror implements StaticFieldValuesMirror {

        @Override
        public ClassMirror getClassMirror() {
            return loadClassMirrorInternal(Type.getType(Object.class));
        }

        public ClassMirror forClassMirror() {
            return BytecodeClassMirror.this;
        }
        
        @Override
        public int identityHashCode() {
            return 0;
        }
        
        @Override
        public Object getBoxedValue(FieldMirror field) {
            return ((BytecodeFieldMirror)field).value;
        }
        
        @Override
        public ObjectMirror get(FieldMirror field) throws IllegalAccessException {
            Object value = getBoxedValue(field);
            if (value == null) {
                return null;
            }
            
            // Value must be string
            return getVM().makeString((String)value);
        }

        @Override
        public boolean getBoolean(FieldMirror field) throws IllegalAccessException {
            return (getBoxedValue(field) == Integer.valueOf(1));
        }
        
        @Override
        public byte getByte(FieldMirror field) throws IllegalAccessException {
            return ((Integer)getBoxedValue(field)).byteValue();
        }
        
        @Override
        public char getChar(FieldMirror field) throws IllegalAccessException {
            return (char)((Integer)getBoxedValue(field)).intValue();
        }
        
        @Override
        public short getShort(FieldMirror field) throws IllegalAccessException {
            return ((Integer)getBoxedValue(field)).shortValue();
        }
        
        @Override
        public void setBoxedValue(FieldMirror field, Object o) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }
    }
    
    private Object convertAnnotationValue(Object value) {
        if (value instanceof Type) {
            return loadClassMirrorInternal((Type)value);
        } else if (value instanceof String[]) {
            String[] array = (String[])value;
            return new AnnotationMirror.EnumMirror(loadClassMirrorInternal(Type.getType(array[0])), array[1]);
        } else if (value instanceof AnnotationNode) {
            return new BytecodeAnnotationMirror((AnnotationNode)value);
        } else if (value instanceof List) {
            List<?> list = (List<?>)value;
            List<Object> result = new ArrayList<Object>();
            for (Object o : list) {
                result.add(convertAnnotationValue(o));
            }
            return result;
        } else if (value instanceof Object[]) {
            return convertAnnotationValue(Arrays.asList((Object[])value));
        } else {
            return value;
        }
    }
    
    public List<AnnotationMirror> wrap(List<AnnotationNode> nodes) {
        List<AnnotationMirror> annotations = new ArrayList<AnnotationMirror>();
        for (AnnotationNode node : nodes) {
            annotations.add(new BytecodeAnnotationMirror(node));
        }
        return annotations;
    }
    
    public List<List<AnnotationMirror>> wrapList(List<List<AnnotationNode>> lists) {
        List<List<AnnotationMirror>> annotations = new ArrayList<List<AnnotationMirror>>();
        for (List<AnnotationNode> nodes : lists) {
            annotations.add(wrap(nodes));
        }
        return annotations;
    }
    
    private class BytecodeAnnotationMirror implements AnnotationMirror {

        private final String desc;
        private ClassMirror klass;
        private final Map<String, Object> values = new HashMap<String, Object>();
        
        public BytecodeAnnotationMirror(AnnotationNode node) {
            this.desc = node.desc;
            
            if (node.values != null) {
                for (int i = 0; i < node.values.size(); i += 2) {
                    String name = (String) node.values.get(i);
                    Object value = node.values.get(i + 1);
                    values.put(name, convertAnnotationValue(value));
                }
            }
//            resolve();
        }

        private void resolve() {
            if (klass == null) {
                klass = loadClassMirrorInternal(Type.getType(desc));
            }
        }
        
        @Override
        public ClassMirror getClassMirror() {
            resolve();
            return klass;
        }
        
        @Override
        public List<String> getKeys() {
            return new ArrayList<String>(values.keySet());
        }

        @Override
        public Object getValue(ThreadMirror thread, String name) {
            if (values.containsKey(name)) {
                return values.get(name);
            } else {
                try {
                    resolve();
                    return klass.getDeclaredMethod(name).getDefaultValue(thread);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    private class BytecodeMethodMirror implements MethodMirror, ConstructorMirror {

        private final MethodNode method;
        private final List<AnnotationMirror> annotations;
        private final List<List<AnnotationMirror>> parameterAnnotations;
        private final Object annotationDefault;
        
        public BytecodeMethodMirror(MethodNode method, List<AnnotationMirror> annotations, 
                List<List<AnnotationMirror>> parameterAnnotations, Object annotationDefault) {
            this.method = method;
            this.annotations = annotations;
            this.parameterAnnotations = parameterAnnotations;
            this.annotationDefault = annotationDefault;
        }
        
        @Override
        public byte[] getBytecode() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public List<AnnotationMirror> getAnnotations(ThreadMirror thread) {
            return annotations;
        }
        
        @Override
        public Object invoke(ThreadMirror thread, ObjectMirror obj,
                Object... args) throws IllegalArgumentException,
                IllegalAccessException, MirrorInvocationTargetException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAccessible(boolean flag) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public InstanceMirror newInstance(ThreadMirror thread, Object... args)
        	throws IllegalAccessException, IllegalArgumentException, MirrorInvocationTargetException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return method.name;
        }

        @Override
        public List<String> getParameterTypeNames() {
            List<String> result = new ArrayList<String>();
            for (Type parameterType : Type.getArgumentTypes(method.desc)) {
                result.add(parameterType.getClassName());
            }
            return result;
        }
        
        @Override
        public List<ClassMirror> getParameterTypes() {
            List<ClassMirror> result = new ArrayList<ClassMirror>();
            for (Type parameterType : Type.getArgumentTypes(method.desc)) {
                result.add(loadClassMirrorInternal(parameterType));
            }
            return result;
        }

        @Override
        public String getReturnTypeName() {
            return Type.getReturnType(method.desc).getClassName();
        }
        
        @Override
        public ClassMirror getReturnType() {
            return loadClassMirrorInternal(Type.getReturnType(method.desc));
        }
        
        @Override
        public List<List<AnnotationMirror>> getParameterAnnotations(ThreadMirror thread) {
            return parameterAnnotations;
        }
        
        @Override
        public Object getDefaultValue(ThreadMirror thread) {
            return annotationDefault;
        }

        @Override
        public ClassMirror getDeclaringClass() {
            return BytecodeClassMirror.this;
        }

        @Override
        public int getModifiers() {
            return method.access;
        }

        @Override
        public List<String> getExceptionTypeNames() {
            List<String> result = new ArrayList<String>();
            for (String exception : method.exceptions) {
                result.add(exception.replace('/', '.'));
            }
            return result;
        }
        
        @Override
        public List<ClassMirror> getExceptionTypes() {
            List<ClassMirror> result = new ArrayList<ClassMirror>();
            for (String exception : method.exceptions) {
                result.add(loadClassMirrorInternal(Type.getObjectType(exception)));
            }
            return result;
        }

        @Override
        public String getSignature() {
            return method.desc;
        }
        
        @Override
        public MirrorLocation locationForBytecodeOffset(int offset) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + ": " + method.name;
        }
    }
    
    private boolean resolved = false;
    
    private int access;
    private String superclassName;
    private ClassMirror superclass;
    private String[] interfaceNames;
    private List<ClassMirror> interfaces;
    private boolean isInterface;
    private String enclosingClassName;
    private ClassMirror enclosingClass;
    private String enclosingMethodName;
    private String enclosingMethodDesc;
    private MethodMirror enclosingMethod;
    private List<BytecodeFieldMirror> fields = new ArrayList<BytecodeFieldMirror>();
    private final List<BytecodeMethodMirror> methods = new ArrayList<BytecodeMethodMirror>();
    private final List<BytecodeAnnotationMirror> annotations = new ArrayList<BytecodeAnnotationMirror>();
    
    private StaticsInfo staticInitInfo;
    
    protected String className;
    private final String internalClassName;

    
    public BytecodeClassMirror(String className) {
        this.className = className;
        this.internalClassName = className.replace('.', '/');
    }
    
    @Override
    public final boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        
        BytecodeClassMirror other = (BytecodeClassMirror)obj;
        ClassMirrorLoader thisLoader = getLoader();
        ClassMirrorLoader otherLoader = other.getLoader();
        return className.equals(other.className) && 
                (thisLoader == null ? otherLoader == null : thisLoader.equals(otherLoader)) &&
                getVM().equals(other.getVM());
    }
    
    @Override
    public final int hashCode() {
        int hash = className.hashCode();
        ClassMirrorLoader loader = getLoader();
        if (loader != null) {
            hash *= loader.hashCode();
        }
        return hash;
    }

    private class BytecodeClassVisitor extends ClassVisitor {

        private ClassWriter classWriter;
        private List<AnnotationNode> annotations;
        
        public BytecodeClassVisitor(ClassReader reader) {
            super(Opcodes.ASM5);
            this.classWriter = new ClassWriter(reader, Opcodes.ASM5);
            this.annotations = new ArrayList<AnnotationNode>();
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            BytecodeClassMirror.this.access = access;
            isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
            superclassName = superName;
            interfaceNames = interfaces;
        }
        
        @Override
        public void visitOuterClass(String owner, String name, String desc) {
            enclosingClassName = owner; 
            enclosingMethodName = name;
            enclosingMethodDesc = desc;
        }
        
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationNode node = new AnnotationNode(desc);
            annotations.add(node);
            return node;
        }
        
        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            BytecodeFieldMirror field = new BytecodeFieldMirror(BytecodeClassMirror.this, access, name, desc, value);
            fields.add(field);
            return null;
        }
        
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
            visitor = new BytecodeMethodVisitor(new MethodNode(access, name, desc, signature, exceptions), 
                    classWriter, visitor);
            if (name.equals("<clinit>")) {
                visitor = new DefaultTrackingMethodVisitor(visitor, access, name, desc, signature, exceptions);
                // Inline subroutines since other pieces of the pipeline can't handle them
                visitor = new JSRInlinerAdapter(visitor, access, name, desc, signature, exceptions);
            }
            return visitor;
        }
        
        @Override
        public void visitEnd() {
            for (AnnotationNode node : annotations) {
                BytecodeClassMirror.this.annotations.add(new BytecodeAnnotationMirror(node));
            }
        }
    }
    
    private class DefaultTrackingMethodVisitor extends MethodNode {
        
        private final DefaultTrackingInterpreter intepreter = new DefaultTrackingInterpreter();
        private final MethodVisitor visitor;
        
        public DefaultTrackingMethodVisitor(MethodVisitor visitor, int access, String name, String desc, String signature, String[] exceptions) {
            super(Opcodes.ASM5, access, name, desc, signature, exceptions);
            this.visitor = visitor;
        }
        
        @Override
        public void visitEnd() {
            DefaultTrackingAnalyzer a = new DefaultTrackingAnalyzer(intepreter);
            try {
                a.analyze(className.replace('.', '/'), this);
                staticInitInfo = intepreter.staticsInfo;
                if (visitor != null) {
                    accept(visitor);
                }
            } catch (Throwable e) {
                if (e instanceof IndexOutOfBoundsException
                        && maxLocals == 0 && maxStack == 0)
                {
                    throw new RuntimeException("Data flow checking option requires valid, non zero maxLocals and maxStack values.");
                }
//                if (frames == null) {
//                    frames = a.getFrames();
//                }
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw, true);
//                printAnalyzerResult(this, frames, pw);
//                pw.close();
                throw new RuntimeException(/*sw.toString(), */e);
            }
        }
    }
    
    private class BytecodeMethodVisitor extends MethodVisitor {

        private final MethodNode method;
        private final List<AnnotationNode> annotationNodes;
        private final List<List<AnnotationNode>> parameterAnnotationNodes;
        private AnnotationNode defaultAnnotationNode;
        
        public BytecodeMethodVisitor(MethodNode method, ClassVisitor classWriter, MethodVisitor methodWriter) {
            super(Opcodes.ASM5);
            this.method = method;
            this.annotationNodes = new ArrayList<AnnotationNode>();
            this.parameterAnnotationNodes = new ArrayList<List<AnnotationNode>>();
            this.defaultAnnotationNode = new AnnotationNode(Opcodes.ASM5, null);
            
            int numParams = Type.getArgumentTypes(method.desc).length;
            for (int i = 0; i < numParams; i++) {
                parameterAnnotationNodes.add(new ArrayList<AnnotationNode>());
            }
        }
        
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationNode node = new AnnotationNode(Opcodes.ASM4, desc);
            annotationNodes.add(node);
            return node;
        }
        
        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            AnnotationNode node = new AnnotationNode(Opcodes.ASM4, desc);
            parameterAnnotationNodes.get(parameter).add(node);
            return node;
        }
        
        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return defaultAnnotationNode;
        }
        
        @Override
        public void visitEnd() {
            Object defaultAnnotationValue = null;
            if (defaultAnnotationNode.values != null && !defaultAnnotationNode.values.isEmpty()) {
                defaultAnnotationValue = convertAnnotationValue(defaultAnnotationNode.values.get(0));
            }
            methods.add(new BytecodeMethodMirror(method, 
                    wrap(annotationNodes), wrapList(parameterAnnotationNodes), defaultAnnotationValue));
        }
    }
    
    public static class StaticsInfo {
        
        private final Map<String, IsDefault> statics;
        private final Set<String> touchedClasses;
        private boolean mayHaveSideEffects = false;
        
        public StaticsInfo() {
            this.statics = new HashMap<String, IsDefault>();
            this.touchedClasses = new HashSet<String>();
        }
        
        public StaticsInfo(StaticsInfo other) {
            this.statics = new HashMap<String, IsDefault>(other.statics);
            this.touchedClasses = new HashSet<String>(other.touchedClasses);
            this.mayHaveSideEffects = other.mayHaveSideEffects;
        }
        
        public boolean putStatic(String name, IsDefault value) {
            IsDefault old = statics.get(name);
            if (old == null) {
                old = IsDefault.UNKNOWN;
            }
            IsDefault merged = old.merge(value);
            boolean changed = (old != merged);
            statics.put(name, merged);
            return changed;
        }
        
        public boolean touchClass(String internalName) {
            return touchedClasses.add(internalName);
        }
        
        public boolean merge(StaticsInfo other) {
            boolean changed = false;
            for (Map.Entry<String, IsDefault> entry : other.statics.entrySet()) {
                changed |= putStatic(entry.getKey(), entry.getValue());
            }
            changed |= touchedClasses.retainAll(other.touchedClasses);
            if (!mayHaveSideEffects && other.mayHaveSideEffects) {
                changed = true;
                mayHaveSideEffects = true;
            }
            return changed;
        }

        public IsDefault isDefault(String staticFieldName) {
            IsDefault result = statics.get(staticFieldName);
            // If the field was never touched, it will still have
            // the default value.
            return result == null ? IsDefault.YES : result;
        }

        public boolean mayHaveSideEffects() {
            return mayHaveSideEffects;
        }

        public Set<String> touchedClasses() {
            return touchedClasses;
        }
    }
    
    private class DefaultTrackingInterpreter extends Interpreter<DefaultTrackingValue> {

        private final BasicVerifier betterVerifier;
        private DefaultTrackingFrame frame;
        private StaticsInfo staticsInfo;
        
        public DefaultTrackingInterpreter() {
            super(Opcodes.ASM5);
            betterVerifier = new BasicVerifier();
        }

        public void setContext(DefaultTrackingFrame frame) {
            this.frame = frame;
        }
        
        @Override
        public DefaultTrackingValue newValue(Type type) {
            return DefaultTrackingValue.fromBasicValue(betterVerifier.newValue(type));
        }

        private IsDefault isDefault(AbstractInsnNode insn) {
            switch (insn.getOpcode()) {
            case Opcodes.ICONST_0:
            case Opcodes.LCONST_0:
            case Opcodes.FCONST_0:
            case Opcodes.DCONST_0:
            case Opcodes.ACONST_NULL:
                return IsDefault.YES;
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.LCONST_1:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.DCONST_1:
            case Opcodes.LDC:
            case Opcodes.NEW:
            case Opcodes.NEWARRAY:
            case Opcodes.ANEWARRAY:
            case Opcodes.MULTIANEWARRAY:
                return IsDefault.NO;
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                return ((IntInsnNode)insn).operand == 0 ? IsDefault.YES : IsDefault.NO;
            case Opcodes.JSR:
                // Probably technically never 0 but doesn't matter.
                return IsDefault.MAYBE;
            case Opcodes.GETSTATIC:
                // Could do inter-class analysis but let's keep this simple for now.
                return IsDefault.MAYBE;
            default:
                // Default to conservative value.
                return IsDefault.MAYBE;
            }
        }
        
        @Override
        public DefaultTrackingValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
            return new DefaultTrackingValue(betterVerifier.newOperation(insn), isDefault(insn));
        }

        @Override
        public DefaultTrackingValue copyOperation(AbstractInsnNode insn, DefaultTrackingValue value) throws AnalyzerException {
            return value;
        }

        @Override
        public DefaultTrackingValue unaryOperation(AbstractInsnNode insn,
                DefaultTrackingValue value) throws AnalyzerException {
            
            if (insn.getOpcode() == Opcodes.PUTSTATIC) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode)insn;
                if (fieldInsnNode.owner.equals(internalClassName)) {
                    frame.statics.putStatic(fieldInsnNode.name, value.isDefaultValue);
                }
            }
            return DefaultTrackingValue.fromBasicValue(betterVerifier.unaryOperation(insn, value.getBasicValue()), isDefault(insn));
        }

        @Override
        public DefaultTrackingValue binaryOperation(AbstractInsnNode insn,
                DefaultTrackingValue value1, DefaultTrackingValue value2)
                throws AnalyzerException {
            return DefaultTrackingValue.fromBasicValue(betterVerifier.binaryOperation(insn, 
                    value1.getBasicValue(), value2.getBasicValue()));
        }

        @Override
        public DefaultTrackingValue ternaryOperation(AbstractInsnNode insn,
                DefaultTrackingValue value1, DefaultTrackingValue value2,
                DefaultTrackingValue value3) throws AnalyzerException {
            return DefaultTrackingValue.fromBasicValue(betterVerifier.ternaryOperation(insn, 
                    value1.getBasicValue(), value2.getBasicValue(), value3.getBasicValue()));
        }

        @Override
        public DefaultTrackingValue naryOperation(AbstractInsnNode insn,
                List<? extends DefaultTrackingValue> values)
                throws AnalyzerException {
            List<BasicValue> basicValues = new ArrayList<BasicValue>(values.size());
            for (DefaultTrackingValue value : values) {
                basicValues.add(value.getBasicValue());
            }
            
            return DefaultTrackingValue.fromBasicValue(betterVerifier.naryOperation(insn, basicValues));
        }

        @Override
        public void returnOperation(AbstractInsnNode insn,
                DefaultTrackingValue value, DefaultTrackingValue expected)
                throws AnalyzerException {
            betterVerifier.returnOperation(insn, value.getBasicValue(), expected.getBasicValue());
        }

        @Override
        public DefaultTrackingValue merge(DefaultTrackingValue v, DefaultTrackingValue w) {
            if (v.equals(w)) {
                return v;
            }
            IsDefault isDefault = v.isDefaultValue.merge(w.isDefaultValue);
            BasicValue value = betterVerifier.merge(v.getBasicValue(), w.getBasicValue());
            return new DefaultTrackingValue(value, isDefault);
        }
    }
    
    private static class DefaultTrackingValue implements Value {

        private final BasicValue value;
        
        private final IsDefault isDefaultValue;
        
        public DefaultTrackingValue(BasicValue value) {
            this(value, IsDefault.MAYBE);
        }
        
        public DefaultTrackingValue(BasicValue value, IsDefault isDefaultValue) {
            this.value = value;
            this.isDefaultValue = isDefaultValue;
        }
        
        public static DefaultTrackingValue fromBasicValue(BasicValue value) {
            return value == null ? null : new DefaultTrackingValue(value);
        }
        
        public static DefaultTrackingValue fromBasicValue(BasicValue value, IsDefault isDefault) {
            return value == null ? null : new DefaultTrackingValue(value, isDefault);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DefaultTrackingValue)) {
                return false;
            }
            
            DefaultTrackingValue other = (DefaultTrackingValue)obj;
            return value.equals(other.value) && isDefaultValue == other.isDefaultValue; 
        }
        
        @Override
        public int hashCode() {
            return value.hashCode();
        }
        
        
        @Override
        public int getSize() {
            return value.getSize();
        }
        
        public BasicValue getBasicValue() {
            return value;
        }
    }
    
    public enum IsDefault { 
        UNKNOWN {
            @Override
            public boolean couldBeDefault() {
                return false;
            }
            @Override
            public boolean couldBeNonDefault() {
                return false;
            }
            @Override
            public IsDefault merge(IsDefault other) {
                return other;
            }
        },
        YES {
            @Override
            public boolean couldBeDefault() {
                return true;
            }
            @Override
            public boolean couldBeNonDefault() {
                return false;
            }
            @Override
            public IsDefault merge(IsDefault other) {
                return (other == YES || other == UNKNOWN) ? YES : MAYBE;
            }
        }, 
        NO {
            @Override
            public boolean couldBeDefault() {
                return false;
            }
            @Override
            public boolean couldBeNonDefault() {
                return true;
            }
            @Override
            public IsDefault merge(IsDefault other) {
                return (other == NO || other == UNKNOWN) ? NO : MAYBE;
            }
        },
        MAYBE {
            @Override
            public boolean couldBeDefault() {
                return true;
            }
            @Override
            public boolean couldBeNonDefault() {
                return true;
            } 
            @Override
            public IsDefault merge(IsDefault other) {
                return this;
            }
        };
            
        public abstract boolean couldBeDefault();
        public abstract boolean couldBeNonDefault();
        public abstract IsDefault merge(IsDefault other);
    };
    
    private class DefaultTrackingAnalyzer extends Analyzer<DefaultTrackingValue> {

        public DefaultTrackingAnalyzer(Interpreter<DefaultTrackingValue> interpreter) {
            super(interpreter);
        }
        
        @Override
        protected Frame<DefaultTrackingValue> newFrame( Frame<? extends DefaultTrackingValue> src) {
            return new DefaultTrackingFrame(src);
        }
        
        @Override
        protected Frame<DefaultTrackingValue> newFrame(int nLocals, int nStack) {
            return new DefaultTrackingFrame(nLocals, nStack);
        }
    }
    
    private class DefaultTrackingFrame extends Frame<DefaultTrackingValue> {

        private final StaticsInfo statics;
        
        public DefaultTrackingFrame(Frame<? extends DefaultTrackingValue> src) {
            super(src);
            statics = new StaticsInfo(((DefaultTrackingFrame)src).statics);
        }
        
        public DefaultTrackingFrame(int nLocals, int nStack) {
            super(nLocals, nStack);
            statics = new StaticsInfo();
        }
        
        @Override
        public boolean merge(Frame<? extends DefaultTrackingValue> frame,
                Interpreter<DefaultTrackingValue> interpreter)
                throws AnalyzerException {
            
            boolean changed = super.merge(frame, interpreter);
            
            DefaultTrackingFrame trackingFrame = (DefaultTrackingFrame)frame;
            changed = changed | statics.merge(trackingFrame.statics);
            
            return changed;
        }
        
        @Override
        public void execute(AbstractInsnNode insn,
                Interpreter<DefaultTrackingValue> interpreter)
                throws AnalyzerException {
            
            DefaultTrackingInterpreter trackingInterpreter = (DefaultTrackingInterpreter)interpreter;
            trackingInterpreter.setContext(this);
            super.execute(insn, interpreter);
            
            switch (insn.getOpcode()) {
            case Opcodes.PUTFIELD:
                statics.mayHaveSideEffects = true;
                break;
            case Opcodes.PUTSTATIC:
                FieldInsnNode fieldInsnNode = (FieldInsnNode)insn;
                if (!fieldInsnNode.owner.equals(internalClassName)) {
                    statics.mayHaveSideEffects = true;
                    statics.touchClass(fieldInsnNode.owner);
                }
                break;
            case Opcodes.INVOKEINTERFACE:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEVIRTUAL:
                MethodInsnNode methodInsnNode = (MethodInsnNode)insn;
                // Class.desiredAssertionStatus() is automatically inserted into any class with assertions,
                // and is known to be side-effect free.
                if (!(methodInsnNode.owner.equals(classType.getInternalName()) && methodInsnNode.name.equals("desiredAssertionStatus"))) {
                    statics.mayHaveSideEffects = true;
                }
                if (!methodInsnNode.owner.equals(internalClassName)) {
                    statics.touchClass(methodInsnNode.owner);
                }
                break;
            case Opcodes.INVOKEDYNAMIC:
                throw new UnsupportedOperationException("TODO");
            case Opcodes.RETURN:
                if (trackingInterpreter.staticsInfo == null) {
                    trackingInterpreter.staticsInfo = new StaticsInfo(statics);
                } else {
                    trackingInterpreter.staticsInfo.merge(statics);
                }
                break;
            }
        }
    }
    
    protected ClassMirror loadClassMirrorInternal(Type type) {
        try {
            return Reflection.classMirrorForType(getVM(), ThreadHolograph.currentThreadMirror(), type, false, getLoader());
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void resolve() {
        if (resolved) {
            return;
        }
        
        ClassReader reader = new ClassReader(getBytecode());
        reader.accept(new BytecodeClassVisitor(reader), 0);
        
        resolved = true;
    }
    
    @Override
    public String getClassName() {
        return className;
    }
    
    @Override
    public String getSignature() {
        return "L" + className.replace('.', '/') + ";";
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return getVM().findBootstrapClassMirror(Class.class.getName());
    }
    
    @Override
    public int identityHashCode() {
        // Should never be called
        throw new UnsupportedOperationException();
    }
    
    @Override
    public abstract byte[] getBytecode();

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public ClassMirror getComponentClassMirror() {
        return null;
    }

    @Override
    public ClassMirror getSuperClassMirror() {
        resolve();
        if (superclassName != null && superclass == null) {
            superclass = loadClassMirrorInternal(Type.getObjectType(superclassName));
        }
        return superclass;
    }

    @Override
    public boolean isInterface() {
        resolve();
        return isInterface;
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        resolve();
        if (interfaces == null) {
            interfaces = new ArrayList<ClassMirror>(interfaceNames.length);
            for (String interfaceName : interfaceNames) {
                interfaces.add(loadClassMirrorInternal(Type.getObjectType(interfaceName)));
            }
        }
        return interfaces;
    }

    @Override
    public List<FieldMirror> getDeclaredFields() {
        resolve();
        return new ArrayList<FieldMirror>(fields);
    }
    
    @Override
    public FieldMirror getDeclaredField(String name) {
        resolve();
        for (BytecodeFieldMirror field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    public boolean methodMatches(MethodMirror m, String name, String... paramClassNames) {
	if (!m.getName().equals(name)) {
            return false;
        }
	return m.getParameterTypeNames().equals(Arrays.asList(paramClassNames));
    }
    
    @Override
    public MethodMirror getDeclaredMethod(String name, String... paramClassNames) 
            throws SecurityException, NoSuchMethodException {
        resolve();
        for (MethodMirror m : methods) {
            if (methodMatches(m, name, paramClassNames)) {
                return m;
            }
        }
        throw new NoSuchMethodException(name);
    }
    
    @Override
    public MethodMirror getMethod(String name, String... paramClassNames)
            throws SecurityException, NoSuchMethodException {
        resolve();
        for (MethodMirror m : methods) {
            if (methodMatches(m, name, paramClassNames)) {
        	return m;
            }
        }

        if (getSuperClassMirror() != null) {
            try {
                return getSuperClassMirror().getMethod(name, paramClassNames);
            } catch (NoSuchMethodException e) {
                // Fall through
            }
        }
        
        for (ClassMirror interfaceMirror : getInterfaceMirrors()) {
            try {
                return interfaceMirror.getMethod(name, paramClassNames);
            } catch (NoSuchMethodException e) {
                // Fall through
            }
        }
        
        throw new NoSuchMethodException(name);
    }
    
    @Override
    public ConstructorMirror getConstructor(String... paramClassNames)
            throws SecurityException, NoSuchMethodException {
        
	resolve();
        for (BytecodeMethodMirror m : methods) {
            if (methodMatches(m, "<init>", paramClassNames)) {
        	return m;
            }
        }

        if (getSuperClassMirror() != null) {
            try {
                return getSuperClassMirror().getConstructor(paramClassNames);
            } catch (NoSuchMethodException e) {
                // Fall through
            }
        }
        
        for (ClassMirror interfaceMirror : getInterfaceMirrors()) {
            try {
                return interfaceMirror.getConstructor(paramClassNames);
            } catch (NoSuchMethodException e) {
                // Fall through
            }
        }
        
        throw new NoSuchMethodException();
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
	resolve();
        List<ConstructorMirror> result = new ArrayList<ConstructorMirror>();
        for (BytecodeMethodMirror m : methods) {
            if (m.getName().equals("<init>")) {
        	result.add(m);
            }
        }
        
        return result;
    }
    
    @Override
    public List<MethodMirror> getDeclaredMethods(boolean publicOnly) {
        resolve();
        List<MethodMirror> result = new ArrayList<MethodMirror>();
        for (BytecodeMethodMirror m : methods) {
            if (!m.getName().equals("<init>")) {
        	result.add(m);
            }
        }
        
        return result;
    }
    
    @Override
    public int getModifiers() {
        resolve();
        return access;
    }
    
    @Override
    public List<ObjectMirror> getInstances() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int size) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ArrayMirror newArray(int... dims) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getClassName();
    }

    public StaticsInfo classInitInfo() {
        resolve();
        return staticInitInfo;
    }

    @Override
    public void bytecodeLocated(File originalBytecodeLocation) {
    }
    
    @Override
    public List<ClassMirror> getSubclassMirrors() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ClassMirror getEnclosingClassMirror() {
        if (enclosingClass == null && enclosingClassName != null) {
            enclosingClass = loadClassMirrorInternal(Type.getObjectType(enclosingClassName));
        }
        return enclosingClass;
    }
    
    @Override
    public MethodMirror getEnclosingMethodMirror() {
        if (enclosingMethod == null && enclosingMethodName != null) {
            try {
                enclosingMethod = Reflection.getDeclaredMethod(getEnclosingClassMirror(), enclosingMethodName, Type.getType(enclosingMethodDesc));
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(e.getMessage());
            }
        }
        return enclosingMethod;
    }
    
    @Override
    public List<AnnotationMirror> getAnnotations(ThreadMirror thread) {
        resolve();
        return new ArrayList<AnnotationMirror>(annotations);
    }
    
    @Override
    public MirrorLocation locationOfLine(int lineNumber) {
        // TODO-RS: For now
        throw new UnsupportedOperationException();
    }
    
    @Override
    public FieldMirror createField(int modifiers, ClassMirror type, String name) {
        throw new UnsupportedOperationException();
    }
}
