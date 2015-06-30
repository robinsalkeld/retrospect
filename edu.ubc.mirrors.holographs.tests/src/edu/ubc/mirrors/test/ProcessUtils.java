package edu.ubc.mirrors.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

    public static Process launchJava(String mainClassName, List<String> programArgs, List<String> vmArgs, List<String> env) throws IOException {
        List<String> commandList = new ArrayList<String>();
        commandList.add("java");
        commandList.addAll(vmArgs);
        commandList.add(mainClassName);
        commandList.addAll(programArgs);
        System.out.println("Launching via exec: " + commandList);
        return Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]), 
                                         env.toArray(new String[env.size()]));
    }
    
    public static void handleStreams(Process process, OutputStream out, OutputStream err) {
        if (out != null) {
            new StreamSiphon(process.getInputStream(), out).start();
        }
        if (err != null) {
            new StreamSiphon(process.getErrorStream(), err).start();
        }
    }
    
    private static class StreamSiphon extends Thread {
        
        public StreamSiphon(InputStream in, OutputStream out) {
            super("StreamSiphon for " + in);
            this.in = in;
            this.out = out;
        }

        private final InputStream in;
        private final OutputStream out;
        
        @Override
        public void run() {
            byte[] buffer = new byte[10];
            int read;
            try {
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
}
