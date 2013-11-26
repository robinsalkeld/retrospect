package edu.ubc.retrospect;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class FakeURLStreamHandler extends URLStreamHandler {

    private final InputStream stream;
    
    public FakeURLStreamHandler(InputStream stream) {
        this.stream = stream;
    }
    
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new FakeURLConnection(u);
    }
    
    private class FakeURLConnection extends URLConnection {
        
        protected FakeURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            return stream;
        }
    }
}
