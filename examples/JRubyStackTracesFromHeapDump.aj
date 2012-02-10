 package examples;

import java.util.Collection;

import org.aspectj.lang.reflect.TypePattern;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyThread;
import org.jruby.runtime.Frame;
import org.jruby.runtime.backtrace.TraceType;
import org.jruby.runtime.builtin.IRubyObject;

public abstract aspect JRubyStackTracesFromHeapDump {

    after(Collection<Ruby> rubies): snapshot() && instances(Ruby, rubies) {
        for (Ruby ruby : rubies) {
            RubyThread[] threads = ruby.getThreadService().getActiveRubyThreads();
            for (RubyThread thread : threads) {
                IRubyObject trace = thread.getContext().createCallerBacktrace(ruby, 0);
                TraceType.dumpCaller((RubyArray)trace);
            }
        }
    }
    
}
