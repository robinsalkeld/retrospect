package edu.ubc.mirrors.holographs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ShortArrayMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.MethodHandle;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.MirageVirtualMachine;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.ArrayClassMirror;
import edu.ubc.mirrors.raw.BytecodeClassMirror;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.nativestubs.java.lang.SystemStubs;
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class VirtualMachineHolograph extends WrappingVirtualMachine {

    private final MirageVirtualMachine mirageVM;
    
    private final MirageClassLoader mirageBootstrapLoader;
    
    private final Thread debuggingThread;
    
    private final Map<String, String> mappedFiles;
    private final ClassLoader bootstrapBytecodeLoader;
    private final Map<String, ClassHolograph> dynamicallyDefinedClasses =
            new HashMap<String, ClassHolograph>();
    
    public Map<Integer, FileInputStream> fileInputStreams = new HashMap<Integer, FileInputStream>();
    
    public Map<Long, ZipFile> zipFilesByAddress = new HashMap<Long, ZipFile>();
    public Map<Long, File> zipPathsByAddress = new HashMap<Long, File>();
    public Map<List<Long>, ZipEntry> zipEntriesByAddresses = new HashMap<List<Long>, ZipEntry>();
    
    public Map<Long, Inflater> inflaterByAddress = new HashMap<Long, Inflater>();
    
    public VirtualMachineHolograph(VirtualMachineMirror wrappedVM, List<URL> bootstrapPath, Map<String, String> mappedFiles) {
        super(wrappedVM);
        this.mirageVM = new MirageVirtualMachine(this);
        this.mirageBootstrapLoader = new MirageClassLoader(this, null);
        this.mappedFiles = mappedFiles;
        this.bootstrapBytecodeLoader = new URLClassLoader(bootstrapPath.toArray(new URL[bootstrapPath.size()]));
        
        // Start a thread dedicated to debugging, so the debugger has something to
        // execute mirror interface methods on without messing up the rest of the VM.
        this.debuggingThread = new HolographDebuggingThread("HolographDebuggingThread");
        this.debuggingThread.setDaemon(true);
        this.debuggingThread.start();
        
        collectZipFiles();
        
    }
    
    private void collectZipFiles() {
        for (ClassMirror zipFileClass : findAllClasses(ZipFile.class.getName(), true)) {
            for (InstanceMirror zipFileMirror : zipFileClass.getInstances()) {
                long address;
                String name;
                try {
                    address = zipFileMirror.getMemberField("jzfile").getLong();
                    name = Reflection.getRealStringForMirror((InstanceMirror)zipFileMirror.getMemberField("name").get());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                if (name != null) {
                    zipPathsByAddress.put(address, new File(name));
                }
            }
        }
    }
    
    public File getMappedFile(InstanceMirror fileMirage, boolean errorOnUnmapped) {
        InstanceMirror pathMirror = (InstanceMirror)Reflection.getField(fileMirage, "path");
        String path = Reflection.getRealStringForMirror(pathMirror);
        return getMappedFile(new File(path), errorOnUnmapped);
    }
    
    public File getMappedFile(File mirrorFile, boolean errorOnUnmapped) {
        String path = mirrorFile.getAbsolutePath();
        for (Map.Entry<String, String> entry : mappedFiles.entrySet()) {
            String key = entry.getKey();
            if (path.startsWith(key)) {
                return new File(entry.getValue() + path.substring(key.length()));
            }
        }
        if (errorOnUnmapped) {
            throw new IllegalArgumentException("Unmapped file path: " + mirrorFile);
        } else {
            return null;
        }
    }
    
    public Inflater getHostInflator(long address) {
        Inflater hostInflater = inflaterByAddress.get(address);
        if (hostInflater == null) {
            // This is likely a pooled, empty inflater.
            // TODO-RS: Need to actually guard against hitting this outside of the
            // controlled read-only mapped file system.
            hostInflater = new Inflater(true);
            inflaterByAddress.put(address, hostInflater);
        }
        return hostInflater;
    }
    
    @Override
    protected ObjectMirror wrapMirror(ObjectMirror mirror) {
        final String internalClassName = mirror.getClassMirror().getClassName();
        
        if (internalClassName.equals("[Z")) {
            return new MutableBooleanArrayMirror(this, (BooleanArrayMirror)mirror);
        } else if (internalClassName.equals("[B")) {
            return new MutableByteArrayMirror(this, (ByteArrayMirror)mirror);
        } else if (internalClassName.equals("[C")) {
            return new MutableCharArrayMirror(this, (CharArrayMirror)mirror);
        } else if (internalClassName.equals("[S")) {
            return new MutableShortArrayMirror(this, (ShortArrayMirror)mirror);
        } else if (internalClassName.equals("[I")) {
            return new MutableIntArrayMirror(this, (IntArrayMirror)mirror);
        } else if (internalClassName.equals("[J")) {
            return new MutableLongArrayMirror(this, (LongArrayMirror)mirror);
        } else if (internalClassName.equals("[F")) {
            return new MutableFloatArrayMirror(this, (FloatArrayMirror)mirror);
        } else if (internalClassName.equals("[D")) {
            return new MutableDoubleArrayMirror(this, (DoubleArrayMirror)mirror);
        } else if (mirror instanceof ObjectArrayMirror) {
            return new MutableObjectArrayMirror(this, (ObjectArrayMirror)mirror);
        } else if (mirror instanceof ClassMirror) {
            return new ClassHolograph(this, (ClassMirror)mirror);
        } else if (mirror instanceof ClassMirrorLoader) {
            return new ClassLoaderHolograph(this, (ClassMirrorLoader)mirror);
        } else if (mirror instanceof ThreadMirror) {
            return new ThreadHolograph(this, (ThreadMirror)mirror);
        } else if (mirror instanceof InstanceMirror) {
            return new InstanceHolograph(this, (InstanceMirror)mirror);
        } else if (mirror.getClassMirror().getClassName().length() == 2) {
            // TODO-RS: wrapping primitive array mirrors
            return super.wrapMirror(mirror);
        } else if (mirror instanceof ObjectArrayMirror) {
            return new ObjectArrayHolograph(this, (ObjectArrayMirror)mirror);
        } else {
            return super.wrapMirror(mirror);
        }
    }
    
    @Override
    protected FieldMirror wrapFieldMirror(FieldMirror fieldMirror) {
        return new MutableFieldMirror(this, fieldMirror);
    }
    
    public MirageClassLoader getMirageClassLoader() {
        return mirageBootstrapLoader;
    }
    
    public MirageVirtualMachine getMirageVM() {
        return mirageVM;
    }
    
    public ClassLoader getBootstrapBytecodeLoader() {
        return bootstrapBytecodeLoader;
    }
    
    public ClassMirror getBytecodeClassMirror(final ClassMirror holographClass) {
        final ClassMirrorLoader holographLoader = holographClass.getLoader();
        
        if (holographClass.isPrimitive()) {
            return holographClass;
        } else if (holographClass.isArray()) {
            return new ArrayClassMirror(1, getBytecodeClassMirror(holographClass.getComponentClassMirror()));
        }
        
        final byte[] bytecode = getBytecode(holographClass);
        return new BytecodeClassMirror(holographClass.getClassName()) {
            @Override
            public byte[] getBytecode() {
                return bytecode;
            }
            @Override
            public VirtualMachineMirror getVM() {
                return VirtualMachineHolograph.this;
            }
            @Override
            public ClassMirrorLoader getLoader() {
                return holographLoader;
            }
            @Override
            protected ClassMirror loadClassMirrorInternal(Type type) {
                try {
                    return Reflection.classMirrorForType(VirtualMachineHolograph.this, type, false, holographLoader);
                } catch (ClassNotFoundException e) {
                    throw new NoClassDefFoundError(e.getMessage());
                }
            }
            
            @Override
            public boolean initialized() {
                return false;
            }
        };
    }
    
    public byte[] getBytecode(ClassMirror holographClass) {
        ClassMirrorLoader holographLoader = holographClass.getLoader();
        if (holographLoader == null) {
            byte[] result = NativeClassMirror.getNativeBytecode(bootstrapBytecodeLoader, holographClass.getClassName());
            if (result == null) {
                throw new InternalError("Couldn't load bytecode for bootstrapped class: " + holographClass);
            }
            return result;
        }
        
        String className = holographClass.getClassName();
        MirageClassLoader mirageLoader = ((ClassLoaderHolograph)holographLoader).getMirageClassLoader();
        if (mirageLoader.myTraceDir != null) {
            File file = mirageLoader.createClassFile(className.replace('.', '/') + ".original.class");
            if (file.exists()) {
                try {
                    return NativeClassMirror.readFully(new FileInputStream(file));
                } catch (Throwable e) {
                    throw new RuntimeException("Error caught while using cached original class definition " + className, e);
                }
            }
        }
        
        if (MirageClassLoader.debug) {
            MirageClassLoader.printIndent();
            System.out.println("Fetching original bytecode for: " + holographClass.getClassName());
        }
        
        String resourceName = className.replace('.', '/') + ".class";
        InstanceMirror resourceNameMirror = Reflection.makeString(this, resourceName);
        InstanceMirror stream = (InstanceMirror)Reflection.invokeMethodHandle(holographLoader, new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((ClassLoader)null).getResourceAsStream((String)null);
            }
        }, resourceNameMirror);
        if (stream == null) {
            throw new InternalError("Couldn't load bytecode for class: " + holographClass);
        }
        MethodHandle readMethod = new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((InputStream)null).read(null, 0, 0);
            }  
        };
        byte[] localBuffer = new byte[4096];
        ByteArrayMirror localBufferMirror = new NativeByteArrayMirror(localBuffer);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayMirror remoteBuffer = (ByteArrayMirror)getPrimitiveClass("byte").newArray(4096);
        int read;
        InstanceMirror verifierStream = (InstanceMirror)Reflection.getField(stream, "in");
        InstanceMirror inflaterStream = (InstanceMirror)Reflection.getField(verifierStream, "is");
        while ((read = (Integer)Reflection.invokeMethodHandle(stream, readMethod, remoteBuffer, 0, remoteBuffer.length())) != -1) {
//            System.out.println(inflaterStream);
            SystemStubs.arraycopyMirrors(remoteBuffer, 0, localBufferMirror, 0, remoteBuffer.length());
            baos.write(localBuffer, 0, read);
        }
        byte[] result = baos.toByteArray();
        
        if (mirageLoader.myTraceDir != null) {
            if (MirageClassLoader.debug) {
                MirageClassLoader.printIndent();
                System.out.println("Caching original bytecode for: " + holographClass.getClassName());
            }
            File file = mirageLoader.createClassFile(className.replace('.', '/') + ".original.class");
            OutputStream classFile;
            try {
                classFile = new FileOutputStream(file);
                classFile.write(result);
                classFile.flush();
                classFile.close();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        return result;
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        ClassMirror result = super.findBootstrapClassMirror(name);
        if (result != null) {
            return result;
        }
        
        result = dynamicallyDefinedClasses.get(name);
        if (result != null) {
            return result;
        }
        
        // If the class wasn't loaded already, imitate what the VM would have done to define it.
        // TODO-RS: Not completely sure how incomplete this is. Class transformers may still
        // apply etc.
        final byte[] bytecode = NativeClassMirror.getNativeBytecode(bootstrapBytecodeLoader, name);
        if (bytecode != null) {
            ClassMirror newClass = new DefinedClassMirror(this, null, name, bytecode);
            ClassHolograph newClassHolograph = (ClassHolograph)getWrappedClassMirror(newClass);
            dynamicallyDefinedClasses.put(name, newClassHolograph);
            return newClassHolograph;
        } 
        
        return null;
    }
    
    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        
        if (findBootstrapClassMirror(name) != null) {
            throw new IllegalArgumentException("Attempt to define already defined class: " + name);
        }
        
        final byte[] realBytecode = new byte[len];
        SystemStubs.arraycopyMirrors(b, off, new NativeByteArrayMirror(realBytecode), 0, len);
        
        ClassMirror newClass = new DefinedClassMirror(this, null, name, realBytecode);
        ClassHolograph newClassHolograph = (ClassHolograph)getWrappedClassMirror(newClass);
        dynamicallyDefinedClasses.put(name, newClassHolograph);
        return newClassHolograph;
    }
}
