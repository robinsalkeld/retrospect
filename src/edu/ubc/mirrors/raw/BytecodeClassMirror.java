package edu.ubc.mirrors.raw;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
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

import edu.ubc.mirrors.ArrayMirror;
import edu.ubc.mirrors.BoxingFieldMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.fieldmap.ClassFieldMirror;
import edu.ubc.mirrors.fieldmap.FieldMapStringMirror;
import edu.ubc.mirrors.mirages.Reflection;

public abstract class BytecodeClassMirror implements ClassMirror {

    public class StaticField extends BoxingFieldMirror {

        private final String name;
        private final ClassMirror type;
        private final int access;
        private final Object value;
        
        public StaticField(int access, String name, ClassMirror type, Object value) {
            this.access = access;
            this.name = name;
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StaticField)) {
                return false;
            }
            
            return name.equals(((StaticField)obj).name);
        }
        
        @Override
        public int hashCode() {
            return 17 + name.hashCode();
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public ClassMirror getType() {
            return type;
        }

        @Override
        public ObjectMirror get() throws IllegalAccessException {
            if (value == null) {
                return null;
            }
            
            // Value must be string
            return new FieldMapStringMirror(getVM(), (String)value);
        }

        @Override
        public void set(ObjectMirror o) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getBoxedValue() throws IllegalAccessException {
            return value;
        }

        @Override
        public void setBoxedValue(Object o) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }

        public int getAccess() {
            return access;
        }

    }

    private static class MethodPlaceholder implements MethodMirror {

        @Override
        public Object invoke(ThreadMirror thread, ObjectMirror obj,
                Object... args) throws IllegalArgumentException,
                IllegalAccessException, InvocationTargetException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAccessible(boolean flag) {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private boolean resolved = false;
    
    private int access;
    private ClassMirror superclassNode;
    private List<ClassMirror> interfaceNodes;
    private boolean isInterface;
    private Map<String, ClassMirror> memberFieldNames = new LinkedHashMap<String, ClassMirror>();
    private Map<String, StaticField> staticFields = new LinkedHashMap<String, StaticField>();
    private final Set<Method> methods = new HashSet<Method>();
    
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
                (thisLoader == null ? otherLoader == null : thisLoader.equals(otherLoader));
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

        public BytecodeClassVisitor() {
            super(Opcodes.ASM4);
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            BytecodeClassMirror.this.access = access;
            isInterface = (Opcodes.ACC_INTERFACE & access) != 0;
            
            superclassNode = superName == null ? null : loadClassMirrorInternal(Type.getObjectType(superName));
            
            interfaceNodes = new ArrayList<ClassMirror>(interfaces.length);
            for (String i : interfaces) {
                interfaceNodes.add(loadClassMirrorInternal(Type.getObjectType(i)));
            }
        }
        
        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if ((Opcodes.ACC_STATIC & access) == 0) {
                memberFieldNames.put(name, loadClassMirrorInternal(Type.getType(desc)));
            } else {
                StaticField field = new StaticField(access, name, loadClassMirrorInternal(Type.getType(desc)), value);
                staticFields.put(name, field);
            }
            return null;
        }
        
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            methods.add(new org.objectweb.asm.commons.Method(name, desc));
            if (name.equals("<clinit>")) {
                final MethodVisitor superVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                final DefaultTrackingInterpreter intepreter = new DefaultTrackingInterpreter();
                
                MethodNode analyzer = new MethodNode(access, name, desc, null, null) {
                    @Override
                    public void visitEnd() {
                        DefaultTrackingAnalyzer a = new DefaultTrackingAnalyzer(intepreter);
                        try {
                             a.analyze(className.replace('.', '/'), this);
                            staticInitInfo = intepreter.staticsInfo;
                            if (superVisitor != null) {
                                accept(superVisitor);
                            }
                        } catch (Throwable e) {
                            if (e instanceof IndexOutOfBoundsException
                                    && maxLocals == 0 && maxStack == 0)
                            {
                                throw new RuntimeException("Data flow checking option requires valid, non zero maxLocals and maxStack values.");
                            }
//                            if (frames == null) {
//                                frames = a.getFrames();
//                            }
//                            StringWriter sw = new StringWriter();
//                            PrintWriter pw = new PrintWriter(sw, true);
//                            printAnalyzerResult(this, frames, pw);
//                            pw.close();
                            throw new RuntimeException(/*sw.toString(), */e);
                        }
                    }
                };
                
                // Inline subroutines since other pieces of the pipeline can't handle them
                return new JSRInlinerAdapter(analyzer, access, name, desc, signature, exceptions);
            }
            return null;
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
            return result == null ? IsDefault.UNKNOWN : result;
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
            super(Opcodes.ASM4);
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
                throw new IllegalArgumentException();
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
            return DefaultTrackingValue.fromBasicValue(betterVerifier.unaryOperation(insn, value.getBasicValue()));
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
                statics.mayHaveSideEffects = true;
                MethodInsnNode methodInsnNode = (MethodInsnNode)insn;
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
            return Reflection.classMirrorForType(getVM(), type, false, getLoader());
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
    
    private void resolve() {
        if (resolved) {
            return;
        }
        
        new ClassReader(getBytecode()).accept(new BytecodeClassVisitor(), 0);
        
        resolved = true;
    }
    
    @Override
    public String getClassName() {
        return className;
    }
    
    @Override
    public ClassMirror getClassMirror() {
        return getVM().findBootstrapClassMirror(Class.class.getName());
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
        return superclassNode;
    }

    @Override
    public boolean isInterface() {
        resolve();
        return isInterface;
    }

    @Override
    public List<ClassMirror> getInterfaceMirrors() {
        resolve();
        return interfaceNodes;
    }

    @Override
    public Map<String, ClassMirror> getDeclaredFields() {
        resolve();
        return Collections.unmodifiableMap(memberFieldNames);
    }
    
    @Override
    public FieldMirror getStaticField(String name) throws NoSuchFieldException {
        resolve();
        FieldMirror result = staticFields.get(name);
        if (result != null) {
            return result;
        } else {
            throw new NoSuchFieldException(name);
        }
    }
    
    public Map<String, StaticField> getStaticFields() {
        return staticFields;
    }
    
    @Override
    public FieldMirror getMemberField(String name) throws NoSuchFieldException {
        return new ClassFieldMirror(this, name);
    }

    @Override
    public List<FieldMirror> getMemberFields() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public MethodMirror getMethod(String name, ClassMirror... paramClasses)
            throws SecurityException, NoSuchMethodException {
        resolve();
        for (Method m : methods) {
            if (!m.getName().equals(name)) {
                continue;
            }
            Type[] paramTypes = m.getArgumentTypes();
            if (paramTypes.length != paramClasses.length) {
                continue;
            }
            for (int i = 0; i < paramTypes.length; i++) {
                if (!paramClasses[i].getClassName().equals(paramTypes[i].getClassName())) {
                    continue;
                }
            }
            return new MethodPlaceholder();
        }

        throw new NoSuchMethodException(name);
    }
    @Override
    public ConstructorMirror getConstructor(ClassMirror... paramTypes)
            throws SecurityException, NoSuchMethodException {
        
        // Could create un-invocable methods, but no use for that yet.
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<ConstructorMirror> getDeclaredConstructors(boolean publicOnly) {
        // Could create un-invocable methods, but no use for that yet.
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int getModifiers() {
        resolve();
        return access;
    }
    
    @Override
    public List<InstanceMirror> getInstances() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public InstanceMirror newRawInstance() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ClassMirrorLoader newRawClassLoaderInstance() {
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
}
