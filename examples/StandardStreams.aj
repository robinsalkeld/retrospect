package examples;

import java.io.PrintStream;

import org.aspectj.lang.Aspects;

/**
 * Abstract aspect that redirects references to the standard out and error streams.
 */
public abstract aspect StandardStreams {

    // The scope of aspects to apply this tranformation to.
    // Expected to be something like "within(Aspect1) || within(Aspect2) || ..."
    // Necessary to ensure consistency with inline weaving 
    // if the original execution contained aspects as well, as 
    // otherwise the scope of this aspect would be too broad.
    protected abstract pointcut withinpureaspects();
    
    // Replace System.out/err with the Aspects versions in all our aspects.
    // Likely needs special treatment in the type system to change the type
    // of System.out in these contexts.
    PrintStream around() : cflow(adviceexecution() && withinpureaspects())
                           && get(static PrintStream java.lang.System.out) {
        return StandardStreams.out;
    }
    PrintStream around() : cflow(adviceexecution() && withinpureaspects())
                           && get(static PrintStream java.lang.System.err) {
        return StandardStreams.err;
    }

    // Standard out and err for aspects - copied to show their types.
    // The stream itself exists in the first region, writes affect the second.
    // TODO: Find a better place in the runtime library?
    static final @Regions({"Aspect", "AspectWorld"}) PrintStream out = Aspects.out;
    static final @Regions({"Aspect", "AspectWorld"}) PrintStream err = Aspects.err;
}
