package edu.ubc.mirrors.test;

public class ThrowableTest extends Throwable {

    static class STE {
	StackTraceElement wrapped;
	@Override
	public String toString() {
	    return wrapped.toString();
	}
    }

    public ThrowableTest fillInSuperStackTrace() {
	super.fillInStackTrace();
	return this;
    }
    
    STE getStackTraceElement(int index) {
	return getStackTraceElement(this, index);
    }
    
    int getStackTraceDepth() {
	return getStackTraceDepth(this);
    }
    
    static STE getStackTraceElement(ThrowableTest tt, int index) {
	STE result = new STE();
	result.wrapped = tt.getSuperStackTrace()[index];
	return result;
    }
    
    static int getStackTraceDepth(ThrowableTest tt) {
	return tt.getSuperStackTrace().length;
    }
    
    StackTraceElement[] getSuperStackTrace() {
	return super.getStackTrace();
    }
    
    private STE[] stackTrace = null;
    
    private synchronized STE[] getOurStackTrace() {
        // Initialize stack trace if this is the first call to this method
        if (stackTrace == null) {
            int depth = getStackTraceDepth();
            stackTrace = new STE[depth];
            for (int i=0; i < depth; i++)
                stackTrace[i] = getStackTraceElement(i);
        }
        return stackTrace;
    }
    
    @Override
    public StackTraceElement[] getStackTrace() {
	STE[] clone = getOurStackTrace().clone();
	StackTraceElement[] result = new StackTraceElement[clone.length];
	for (int i = 0; i < result.length; i++) {
	    result[i] = clone[i].wrapped;
	}
	return result;
    }
    
    public static void main(String[] args) throws Throwable {
	Throwable t = foo();
	t.printStackTrace();
	t.fillInStackTrace();
	t.printStackTrace();
    }
    
    private static Throwable foo() throws Throwable {
	return bar();
    }
    
    private static Throwable bar() throws Throwable {
	return new RuntimeException();
    }
}
