package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;

import edu.ubc.util.Stopwatch;

public class LeapUtils {

    private static final String LEAP_MAIN_CLASS = "edu.hkust.leap.Main";
    private static final String GENERATED_REPLAY_CLASS = "replaydriver.ReplayDriver";
    
    private static final File RUNTIME_CLASS_DIR = new File("tmp", "runtime");
    private static final File REPLAY_CLASS_DIR = new File("tmp", "replay");
    private static final File GENERATED_SRC = new File("src");
    private static final File REPLAY_DRIVER_CLASS_FILE = new File(new File(GENERATED_SRC, "replaydriver"), "ReplayDriver.java");
    
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 20; i++) {
            ProcessUtils.waitForWithEcho(ProcessUtils.launchJava("example.Example", 
                    Collections.<String>emptyList(), 
                    Arrays.asList("-cp", EvalConstants.LeapExampleBin.toString()), 
                    Collections.<String>emptyList()));
        }
        
//        File leapDir = new File(EvalConstants.LeapExampleRoot, "leap");
//        leapDir.mkdir();
//        transform("example.Example", EvalConstants.LeapExampleBin.toString(), leapDir);
//        record("example.Example", leapDir);
//        replay("example.Example", leapDir);
        
//        transform("tracing.ExampleMain", EvalConstants.TracingExampleBin.toString(), null);
//        record("tracing.ExampleMain", new File(EvalConstants.TracingExampleRoot, "leap"));
    }
    
    public static void transform(String mainClassName, String classPath, File outputDir) throws IOException {
        Process p = ProcessUtils.launchJava(LEAP_MAIN_CLASS, 
                                Collections.singletonList(mainClassName), 
                                Arrays.asList("-cp", classPath + ":" + EvalConstants.LeapTransformerJar), 
                                Collections.<String>emptyList(),
                                outputDir);
        ProcessUtils.waitForSuccessWithEcho(p);
        // As instructed at http://www.cse.ust.hk/prism/leap/,
        // delete the transformed Leap classes
        recursiveDelete(new File(outputDir, "tmp/replay/edu/hkust").toPath());
        recursiveDelete(new File(outputDir, "tmp/runtime/edu/hkust").toPath());
    }

    public static void recursiveDelete(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override 
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override 
            public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return handleException(e);
            }

            private FileVisitResult handleException(final IOException e) {
                throw new RuntimeException(e);
            }

            @Override 
            public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
                if (e != null) {
                    return handleException(e);
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    };

    public static void record(String mainClassName, String classPath, File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            transform(mainClassName, classPath, outputDir);
        }
        
        File runtimeClassPath = new File(outputDir, RUNTIME_CLASS_DIR.toString());
        Stopwatch s = new Stopwatch();
        s.start();
        Process p = ProcessUtils.launchJava(LEAP_MAIN_CLASS,
                                // TODO-RS: The 1 here is supposed to be the SPE value output from the transformation.
                                Arrays.asList("1", mainClassName), 
                                Arrays.asList("-cp", runtimeClassPath + ":" + EvalConstants.LeapRecorderJar), 
                                Collections.<String>emptyList(),
                                outputDir);
        ProcessUtils.waitForSuccessWithEcho(p);
        long time = s.stop();
        System.out.println("Leap record time: " + time / 1000.0);
    }
    
    public static void forceRecord(String mainClassName, File outputDir) throws IOException {
        
    }
    
    public static void replay(String mainClassName, File outputDir) throws IOException {
        File replayClassPath = new File(outputDir, REPLAY_CLASS_DIR.toString());
        File generatedReplayClassFile = new File(outputDir, REPLAY_DRIVER_CLASS_FILE.toString());
        Process compiler = ProcessUtils.launchJavac(generatedReplayClassFile, 
                Arrays.asList("-cp", replayClassPath + ":" + EvalConstants.LeapReplayerJar), 
                Collections.<String>emptyList());
        ProcessUtils.waitForSuccessWithEcho(compiler);
        
        File generatedSourcePath = new File(outputDir, GENERATED_SRC.toString());
        Process p = ProcessUtils.launchJava(GENERATED_REPLAY_CLASS, 
                                Collections.<String>emptyList(), 
                                Arrays.asList("-cp", generatedSourcePath + ":" + 
                                                     replayClassPath + ":" +
                                                     EvalConstants.LeapReplayerJar), 
                                Collections.<String>emptyList(),
                                outputDir);
        ProcessUtils.waitForSuccessWithEcho(p);
    }
    
}
