package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyException;
import org.jruby.RubyThread;
import org.jruby.internal.runtime.ThreadService;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.backtrace.BacktraceData;
import org.jruby.runtime.backtrace.TraceType;
import org.jruby.runtime.backtrace.TraceType.Gather;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyStackTraces {

    public static String printStackTraces(Ruby runtime) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        
        out.println("Ruby Thread Dump");
        final ThreadService threadService = runtime.getThreadService();
        RubyThread[] thrs = threadService.getActiveRubyThreads();
        for (RubyThread th : thrs) {
            out.println("\n" + th);
            RubyException exc = new RubyException(runtime, runtime.getRuntimeError(), "");
            ThreadContext tc = threadService.getThreadContextForThread(th);
            if (tc != null) {
                exc.setBacktraceData(new BacktraceData(th.javaBacktrace(), tc.createBacktrace2(0, false), false, false, Gather.NORMAL));
                exc.printBacktrace(out);
            } else {
                out.println("    [no longer alive]");
            }
        }
        return new String(bytes.toByteArray());
    }
    
}
