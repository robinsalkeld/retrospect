package edu.ubc.mirrors.test;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyThread;
import org.jruby.runtime.backtrace.TraceType;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyStackTraces {

    public static void printStackTraces(Ruby ruby) {
        RubyThread[] threads = ruby.getThreadService().getActiveRubyThreads();
        for (RubyThread thread : threads) {
            IRubyObject trace = thread.getContext().createCallerBacktrace(ruby, 0);
            TraceType.dumpCaller((RubyArray)trace);
        }
    }
    
}
