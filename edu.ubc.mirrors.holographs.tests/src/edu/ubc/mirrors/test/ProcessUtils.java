package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aspectj.weaver.World;

import edu.ubc.retrospect.MirrorWorld;
import edu.ubc.util.Stopwatch;

public class ProcessUtils {

    public static long timeLoadTimeWeaving(String mainClassName, String classPath, String aspectPath) throws IOException {
        Stopwatch s = new Stopwatch();
        s.start();
        Process p = launchLoadTimeWeaving(mainClassName, classPath, aspectPath);
        waitForSuccessWithEcho(p);
        long time = s.stop();
        System.out.println("LTW run time: " + time / 1000.0);
        return time;
    }
    
    public static File aspectWeaverJar = EvalConstants.getBundleRoot(World.class);
    public static File aspectRuntimeJar = new File(EvalConstants.getBundleRoot(MirrorWorld.class), "lib/aspectjrt-1.7.3.jar");
    public static URL aspectRuntimeJarPath;
    static {
    	try {
    	    aspectRuntimeJarPath = new URL("jar:file://" + aspectRuntimeJar + "!/");
    	} catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Process launchLoadTimeWeaving(String mainClassName, String classPath, 
            String aspectPath) throws IOException {
        
        List<String> programArgs = Collections.emptyList();
        
        List<String> vmArgs = new ArrayList<String>();
        vmArgs.add("-cp");
        vmArgs.add(classPath + ":" + aspectPath + ":" + aspectRuntimeJar);
        vmArgs.add("-javaagent:" + aspectWeaverJar);
        
        List<String> env = Collections.<String>emptyList();
        
        return launchJava(mainClassName, programArgs, vmArgs, env, null);
    }
    
    public static long timeJava(String mainClassName, List<String> programArgs, List<String> vmArgs, List<String> env) throws IOException {
        Stopwatch s = new Stopwatch();
        s.start();
        Process p = launchJava(mainClassName, programArgs, vmArgs, env, null);
        waitForSuccessWithEcho(p);
        long time = s.stop();
        System.out.println("Java run time: " + time / 1000.0);
        return time;
    }
    
    public static Process launchJava(String mainClassName, List<String> programArgs, List<String> vmArgs, List<String> env) throws IOException {
        return launchJava(mainClassName, programArgs, vmArgs, env, null);
    }
    
    public static Process launchJava(String mainClassName, List<String> programArgs, List<String> vmArgs, List<String> env, File currentDir) throws IOException {
        List<String> commandList = new ArrayList<String>();
        commandList.add("java");
        commandList.addAll(vmArgs);
        commandList.add(mainClassName);
        commandList.addAll(programArgs);
        System.out.println("Launching via exec: " + commandList);
        System.out.println("Current dir: " + currentDir);
        return Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]), 
                                         env.toArray(new String[env.size()]),
                                         currentDir);
    }
    
    public static Process launchJavac(File javaFileName, List<String> programArgs, List<String> env) throws IOException {
        List<String> commandList = new ArrayList<String>();
        commandList.add("javac");
        commandList.addAll(programArgs);
        commandList.add(javaFileName.toString());
        System.out.println("Launching via exec: " + commandList);
        return Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]), 
                                         env.toArray(new String[env.size()]));
    }
    
    public static void waitForSuccessWithEcho(Process p) {
        int result = waitForWithEcho(p);
        if (result != 0) {
            throw new RuntimeException("Process returned non-zero exit code: " + result);
        }
    }
    
    public static int waitForWithEcho(Process p) {
        handleStreams(p, System.out, System.err);
        try {
            return p.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
