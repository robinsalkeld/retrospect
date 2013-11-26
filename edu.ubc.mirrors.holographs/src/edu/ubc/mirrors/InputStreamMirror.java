package edu.ubc.mirrors;

import java.io.IOException;
import java.io.InputStream;

import edu.ubc.mirrors.raw.NativeByteArrayMirror;

public class InputStreamMirror extends InputStream {

    private ThreadMirror thread;
    private InstanceMirror streamMirror;
    private ByteArrayMirror remoteBuffer;
    private ClassMirror inputStreamClassMirror;
    private MethodMirror readMethod;
    private MethodMirror readBlockMethod;

    public InputStreamMirror(ThreadMirror thread, InstanceMirror streamMirror) {
        this.thread = thread;
        this.streamMirror = streamMirror;
        this.inputStreamClassMirror = streamMirror.getClassMirror().getVM().findBootstrapClassMirror(InputStream.class.getName());
    }

    
    @Override
    public int read() throws IOException {
        if (readMethod == null) {
            try {
                readMethod = inputStreamClassMirror.getDeclaredMethod("read");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        try {
            return (Integer)readMethod.invoke(thread, streamMirror);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (readBlockMethod == null) {
            try {
                readBlockMethod = inputStreamClassMirror.getDeclaredMethod("read", "byte[]", "int", "int");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        
        ByteArrayMirror localBufferMirror = new NativeByteArrayMirror(b);
        if (remoteBuffer == null || len > remoteBuffer.length()) {
            remoteBuffer = (ByteArrayMirror)streamMirror.getClassMirror().getVM().getPrimitiveClass("byte").newArray(len);
        }
        int read;
        try {
            read = (Integer)readBlockMethod.invoke(thread, streamMirror, remoteBuffer, 0, len);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new IOException(e);
        }
        if (read > 0) {
            Reflection.arraycopy(remoteBuffer, 0, localBufferMirror, off, read);
        }
        return read;
    }
    
    @Override
    public void close() {
        inputStreamClassMirror = null;
        readBlockMethod = null;
        readMethod = null;
        remoteBuffer = null;
        streamMirror = null;
        thread = null;
    }
}
