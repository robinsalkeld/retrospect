package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyException;
import org.jruby.RubyModule;
import org.jruby.RubyThread;
import org.jruby.internal.runtime.ThreadService;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.backtrace.BacktraceData;
import org.jruby.runtime.backtrace.BacktraceElement;
import org.jruby.runtime.backtrace.TraceType;
import org.jruby.runtime.backtrace.TraceType.Gather;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyStackTraces {

  public static ByteArrayOutputStream redirectStdErr() {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      System.setErr(ps);
      return baos;
  }
    
  public static void printStackTraces(Ruby runtime) {
    // Copied verbatim from JRuby 
    // thread dump signal implementation
    System.err.println("Ruby Thread Dump");
    final ThreadService threadService = runtime.getThreadService();
    RubyThread[] thrs = threadService.getActiveRubyThreads();
    for (RubyThread th : thrs) {
      System.err.println("\n" + th);
      RubyException exc = new RubyException(runtime, 
              runtime.getRuntimeError(), "");
      ThreadContext tc = threadService.getThreadContextForThread(th);
      if (tc != null) {
        StackTraceElement[] javaBacktrace = th.javaBacktrace();
        BacktraceElement[] rubyBacktrace = tc.createBacktrace2(0, false);
        exc.setBacktraceData(new BacktraceData(javaBacktrace, rubyBacktrace, 
                false, false, Gather.NORMAL));
        exc.printBacktrace(System.err);
      } else {
        System.err.println("    [no longer alive]");
      }
    }
  }
  
  public static boolean isModuleOrphaned(RubyModule module) {
      Ruby runtime = module.getRuntime();
      RubyModule rootNamespace = runtime.getObject();
      String moduleName = module.getName();
      IRubyObject moduleAtModuleName = rootNamespace.getConstant(moduleName);
      return moduleAtModuleName != module;
  }
}
