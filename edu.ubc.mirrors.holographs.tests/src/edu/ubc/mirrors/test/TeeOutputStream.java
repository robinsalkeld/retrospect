package edu.ubc.mirrors.test;

import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream {

    private final OutputStream out1;
    private final OutputStream out2;
    
    public TeeOutputStream(OutputStream out1, OutputStream out2) {
        this.out1 = out1;
        this.out2 = out2;
    }

    @Override
    public void close() throws IOException {
        try {
            out1.close();
        } finally {
            out2.close();
        }
    }
    
    @Override
    public void flush() throws IOException {
        try {
            out1.flush();
        } finally {
            out2.flush();
        }
    }
    
    @Override
    public void write(int b) throws IOException {
        try {
            out1.write(b);
        } finally {
            out2.write(b);
        }
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            out1.write(b, off, len);
        } finally {
            out2.write(b, off, len);
        }
    }
}
