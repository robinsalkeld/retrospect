package edu.ubc.retrospect;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class FakeURLStreamHandler extends URLStreamHandler {

    private final InputStream stream;
    
    private static int nextSuffix = 0;
    private int suffix = nextSuffix++;
    
    public FakeURLStreamHandler(InputStream stream) {
        this.stream = stream;
    }
    
    public static URL makeURL(InputStream stream) {
        return new FakeURLStreamHandler(stream).getURL();
    }
    
    public URL getURL() {
        try {
            return new URL("fake", "", 0, "" + suffix, this);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
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
