package edu.ubc.aspects;

import java.io.IOException;
import java.io.OutputStream;

public class WormholeStream extends OutputStream {

    private final int fd;
    
    public WormholeStream(int fd) {
        this.fd = fd;
    }
    
    @Override
    public void write(int b) throws IOException {
        write(new byte[]{ (byte)b }, 0, 1);
    }
    
    @Override
    public native void write(byte[] b, int off, int len) throws IOException;
}
