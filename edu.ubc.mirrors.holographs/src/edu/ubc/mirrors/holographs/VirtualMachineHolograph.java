package edu.ubc.mirrors.holographs;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.jar.JarFile;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.Type;

import sun.misc.FileURLMapper;
import sun.misc.Launcher;
import edu.ubc.mirrors.BooleanArrayMirror;
import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.CharArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ClassMirrorPrepareEvent;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.DoubleArrayMirror;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorSetEvent;
import edu.ubc.mirrors.FieldMirrorSetRequest;
import edu.ubc.mirrors.FloatArrayMirror;
import edu.ubc.mirrors.FrameMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.IntArrayMirror;
import edu.ubc.mirrors.LongArrayMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequestManager;
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
import edu.ubc.mirrors.wrapping.WrappingVirtualMachine;

public class VirtualMachineHolograph extends WrappingVirtualMachine {

    private final MirageVirtualMachine mirageVM;
    
    // TODO-RS: Expose this more generally?
    private final EventDispatch dispatch;
    
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
    
    public VirtualMachineHolograph(VirtualMachineMirror wrappedVM, Map<String, String> mappedFiles) {
        super(wrappedVM);
        if (MirageClassLoader.debug) {
            System.out.println("Creating VM holograph...");
        }
        
        this.mirageVM = new MirageVirtualMachine(this);
        this.mirageBootstrapLoader = new MirageClassLoader(this, null);
        this.mappedFiles = mappedFiles;
        this.dispatch = new EventDispatch(this);
        
        List<URL> bootstrapPath = extractBootstrapPath(wrappedVM);
        List<URL> filteredURLs = new ArrayList<URL>();
        for (URL url : bootstrapPath) {
            // Ignore invalid paths as the VM would
            if (new FileURLMapper(url).exists()) {
        	filteredURLs.add(url);
            }
        }
        this.bootstrapBytecodeLoader = new URLClassLoader(filteredURLs.toArray(new URL[filteredURLs.size()]));
        
        // Start a thread dedicated to debugging, so the debugger has something to
        // execute mirror interface methods on without messing up the rest of the VM.
        this.debuggingThread = new HolographDebuggingThread("HolographDebuggingThread");
        this.debuggingThread.setDaemon(true);
        this.debuggingThread.start();
        
        collectZipFiles();
        
        if (MirageClassLoader.debug) {
            System.out.println("Done.");
        }
    }
    
    public EventDispatch dispatch() {
	return dispatch;
    }
    
    private List<URL> extractBootstrapPath(VirtualMachineMirror wrappedVM) {
	try {
	    ClassMirror launcherClass = wrappedVM.findBootstrapClassMirror(Launcher.class.getName());
	    InstanceMirror bootClassPathMirror = (InstanceMirror)launcherClass.getStaticFieldValues().get(launcherClass.getDeclaredField("bootClassPath"));
	    ClassMirror fileClass = wrappedVM.findBootstrapClassMirror(File.class.getName());
	    char pathSeparator = fileClass.getStaticFieldValues().getChar(fileClass.getDeclaredField("pathSeparatorChar"));
	    String bootClassPath = Reflection.getRealStringForMirror(bootClassPathMirror);
	    String[] paths = bootClassPath.split("" + pathSeparator);
	    List<URL> urls = new ArrayList<URL>();
	    for (int i = 0; i < paths.length; i++) {
	        File mappedFile = getMappedFile(new File(paths[i]), true);
	        urls.add(mappedFile.toURI().toURL());
	    }
	    return urls;
	} catch (MalformedURLException e) {
	    throw new RuntimeException(e);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	} catch (NoSuchFieldException e) {
	    throw new RuntimeException(e);
	}
    }

    private void collectZipFiles() {
        try {
            Reflection.withThread(getThreads().get(0), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    ClassMirror zipFileClass = findBootstrapClassMirror(ZipFile.class.getName());
                    for (ClassMirror zipFileSubclass : findAllClasses(ZipFile.class.getName(), true)) {
                        for (InstanceMirror zipFileMirror : zipFileSubclass.getInstances()) {
                            long address;
                            String name;
                            try {
                                address = zipFileMirror.getLong(zipFileClass.getDeclaredField("jzfile"));
                                name = Reflection.getRealStringForMirror((InstanceMirror)HolographInternalUtils.getField(zipFileMirror, "name"));
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

                    if (canBeModified()) {
                        MirrorEventRequestManager erm = eventRequestManager();
                        ClassMirrorPrepareRequest request = erm.createClassMirrorPrepareRequest();
                        request.addClassFilter(ZipFile.class.getName());
                        dispatch.addCallback(request, ZIP_FILE_CREATED_CALLBACK);
                        request.enable();
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private final EventDispatch.EventCallback ZIP_FILE_CREATED_CALLBACK = new EventDispatch.EventCallback() {
	public void handle(MirrorEvent event) {
	    if (event instanceof FieldMirrorSetEvent) {
    	    	FieldMirrorSetEvent fieldSetEvent = (FieldMirrorSetEvent)event;
    	    	if (fieldSetEvent.classMirror().getClassName().equals(ZipFile.class.getName()) && fieldSetEvent.fieldName().equals("name")) {
    	    	    InstanceMirror zipFileMirror = fieldSetEvent.instance();
    	    	    try {
			long address = zipFileMirror.getLong(fieldSetEvent.classMirror().getDeclaredField("jzfile"));
			String path = Reflection.getRealStringForMirror((InstanceMirror)fieldSetEvent.newValue());
			zipPathsByAddress.put(address, new File(path));
		    } catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		    } catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		    }
    	    	}
	    } else if (event instanceof ClassMirrorPrepareEvent) {
		ClassMirror zipFileClass = ((ClassMirrorPrepareEvent)event).classMirror();
		FieldMirrorSetRequest request = eventRequestManager().createFieldMirrorSetRequest(zipFileClass, "name");
	        dispatch.addCallback(request, this);
	        request.enable();
	    }
	}
    };
    
    public ZipFile getZipFileForAddress(long jzfile) {
        ZipFile hostZipFile = zipFilesByAddress.get(jzfile);
        if (hostZipFile == null) {
            File path = zipPathsByAddress.get(jzfile);
            if (path == null) {
                throw new InternalError();
            }
            File mappedPath = getMappedFile(path, true);
            // Create a JarFile in case any of its native methods are invoked
            try {
                hostZipFile = new JarFile(mappedPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return hostZipFile;
    }
    
    public File getMappedFile(InstanceMirror fileMirage, boolean errorOnUnmapped) {
        InstanceMirror pathMirror = (InstanceMirror)HolographInternalUtils.getField(fileMirage, "path");
        String path = Reflection.getRealStringForMirror(pathMirror);
        return getMappedFile(new File(path), errorOnUnmapped);
    }
    
    public File getMappedFile(File mirrorFile, boolean errorOnUnmapped) {
        // TODO-RS: getAbsolutePath() is not right, as "." will be expanded incorrectly!
        // We need to handle ".." without that side-effect somehow.
        String path = mirrorFile.getPath();
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
        ClassHolograph klass = (ClassHolograph)getWrappedClassMirror(fieldMirror.getDeclaringClass());
        return new MutableFieldMirror(klass, fieldMirror);
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
                    return Reflection.classMirrorForType(VirtualMachineHolograph.this, ThreadHolograph.currentThreadMirror(), type, false, holographLoader);
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
        
        ThreadHolograph.raiseMetalevel();
        
        String resourceName = className.replace('.', '/') + ".class";
        InstanceMirror resourceNameMirror = Reflection.makeString(this, resourceName);
        // This isn't a "legit" execution, so cheat and choose a thread if we're not already in holograph execution.
        ThreadMirror thread = ThreadHolograph.currentThreadMirror.get();
        if (thread == null) {
            thread = getThreads().get(0);
        }
        
        InstanceMirror stream = (InstanceMirror)Reflection.invokeMethodHandle(holographLoader, thread, new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((ClassLoader)null).getResourceAsStream((String)null);
            }
        }, resourceNameMirror);
        if (stream == null) {
            throw new InternalError("Couldn't load bytecode for class " + resourceName + " from loader: " + holographLoader);
        }
        
        byte[] result = readBytecodeFromStream(thread, holographClass, stream);
        
        ThreadHolograph.lowerMetalevel();
        
        return result;
    }
    
    private byte[] readBytecodeFromStream(ThreadMirror thread, ClassMirror holographClass, InstanceMirror stream) {
        String className = stream.getClassMirror().getClassName();
        try {
            // Optimizations - if we get back a known InputStream subtype, pull directly from the mapped host file/zip/etc,
            // since going through holograph execution can be pretty slow for such low-level IO.
            if (className.equals(BufferedInputStream.class.getName())) {
                ClassMirror fisClass = findBootstrapClassMirror(FilterInputStream.class.getName());
                InstanceMirror in = (InstanceMirror)stream.get(fisClass.getDeclaredField("in"));
                return readBytecodeFromStream(thread, holographClass, in);
            } else if (className.equals(FileInputStream.class.getName())) {
                InstanceMirror fileDescriptor = (InstanceMirror)stream.get(stream.getClassMirror().getDeclaredField("fd"));
                int fd = fileDescriptor.getInt(fileDescriptor.getClassMirror().getDeclaredField("fd"));
                InputStream in = fileInputStreams.get(fd);
                return NativeClassMirror.readFully(in);
            } else if (className.equals("org.eclipse.osgi.baseadaptor.bundlefile.ZipBundleEntry$ZipBundleEntryInputStream")) {
                InstanceMirror wrapped = (InstanceMirror)stream.get(stream.getClassMirror().getDeclaredField("stream"));
                return readBytecodeFromStream(thread, holographClass, wrapped);
//            } else if (className.equals("java.util.zip.ZipFile$ZipFileInflaterInputStream")) {
//                InstanceMirror jarFile = (InstanceMirror)stream.get(stream.getClassMirror().getDeclaredField("this$0"));
//                ClassMirror zipFileClass = findBootstrapClassMirror(ZipFile.class.getName());
//                String fileName = Reflection.getRealStringForMirror((InstanceMirror)jarFile.get(zipFileClass.getDeclaredField("name")));
//               
//                InstanceMirror zipFileInputStream = (InstanceMirror)stream.get(stream.getClassMirror().getDeclaredField("zfin"));
//                long jzentry = zipFileInputStream.getLong(zipFileInputStream.getClassMirror().getDeclaredField("jzentry"));
//                ZipEntry entry = zipEntriesByAddresses.get(jzentry);
//                
//                ZipFile mappedZipFile = new ZipFile(getMappedFile(new File(fileName), true));
//                InputStream in = mappedZipFile.getInputStream(entry);
//                return NativeClassMirror.readFully(in);
            } else if (className.equals("sun.net.www.protocol.jar.JarURLConnection$JarURLInputStream")) {
                InstanceMirror connection = (InstanceMirror)stream.get(stream.getClassMirror().getDeclaredField("this$0"));
                InstanceMirror jarFile = (InstanceMirror)connection.get(connection.getClassMirror().getDeclaredField("jarFile"));
                ClassMirror zipFileClass = findBootstrapClassMirror(ZipFile.class.getName());
                String fileName = Reflection.getRealStringForMirror((InstanceMirror)jarFile.get(zipFileClass.getDeclaredField("name")));
               
                InstanceMirror jarEntry = (InstanceMirror)connection.get(connection.getClassMirror().getDeclaredField("jarEntry"));
                ClassMirror zipEntryClass = findBootstrapClassMirror(ZipEntry.class.getName());
                String entryName = Reflection.getRealStringForMirror((InstanceMirror)jarEntry.get(zipEntryClass.getDeclaredField("name")));
                
                JarFile mappedJarFile = new JarFile(getMappedFile(new File(fileName), true));
                InputStream in = mappedJarFile.getInputStream(mappedJarFile.getEntry(entryName));
                return NativeClassMirror.readFully(in);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        if (MirageClassLoader.debug) {
            MirageClassLoader.printIndent();
            System.out.println("Fetching original bytecode for: " + holographClass.getClassName() + " (from instance of " + className + ")");
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
        while ((read = (Integer)Reflection.invokeMethodHandle(stream, thread, 
                readMethod, remoteBuffer, 0, remoteBuffer.length())) != -1) {
            Reflection.arraycopy(remoteBuffer, 0, localBufferMirror, 0, remoteBuffer.length());
            baos.write(localBuffer, 0, read);
        }
        byte[] result = baos.toByteArray();

        if (MirageClassLoader.debug) {
            MirageClassLoader.printIndent();
            System.out.println("Fetched original bytecode for: " + holographClass.getClassName());
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
            
            DefinedClassMirror.registerPrepareCallback(newClassHolograph);
            
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
        Reflection.arraycopy(b, off, new NativeByteArrayMirror(realBytecode), 0, len);
        
        ClassMirror newClass = new DefinedClassMirror(this, null, name, realBytecode);
        ClassHolograph newClassHolograph = (ClassHolograph)getWrappedClassMirror(newClass);
        dynamicallyDefinedClasses.put(name, newClassHolograph);
        return newClassHolograph;
    }
    
    @Override
    public MethodMirror wrapMethod(MethodMirror method) {
	MethodMirror wrappedMethod = super.wrapMethod(method);
	ClassHolograph klass = (ClassHolograph)wrappedMethod.getDeclaringClass();
        return new MethodHolograph(klass, wrappedMethod);
    }
    
    public ConstructorMirror wrapConstructor(ConstructorMirror constructor) {
	ConstructorMirror wrappedConstructor = super.wrapConstructor(constructor);
	ClassHolograph klass = (ClassHolograph)wrappedConstructor.getDeclaringClass();
        return new ConstructorHolograph(klass, wrappedConstructor);
    };
    
    @Override
    public boolean canBeModified() {
        return wrappedVM.canBeModified();
    }
 
    @Override
    public FrameMirror wrapFrameMirror(WrappingVirtualMachine vm, FrameMirror frame) {
        return new FrameHolograph(vm, frame);
    }
}
