
package examples;

abstract aspect Trace {

    protected static int callDepth = 0;

    protected static void traceEntry(String str, Object o) {
        callDepth++;
        printEntering(str + ": " + o.toString());
    }
    protected static void traceExit(String str, Object o) {
        printExiting(str + ": " + o.toString());
        callDepth--;
    }
    private static void printEntering(String str) {
        printIndent();
        System.out.println("--> " + str);
    }
    private static void printExiting(String str) {
        printIndent();
        System.out.println("<-- " + str);
    }
    private static void printIndent() {
        for (int i = 0; i < callDepth; i++)
            System.out.print("  ");
    }

    // Application classes - left unspecified.
    abstract pointcut myClass(Object obj);
    // The constructors in those classes.
    pointcut myConstructor(Object obj): myClass(obj) && execution(new(..));
    // The methods of those classes.
    pointcut myMethod(Object obj): myClass(obj) && 
        execution(* *(..)) && !cflow(execution(String toString()));

    before(Object obj): myConstructor(obj) {
        traceEntry("" + thisJoinPointStaticPart.getSignature(), obj);
    }
    after(Object obj): myConstructor(obj) {
        traceExit("" + thisJoinPointStaticPart.getSignature(), obj);
    }

    before(Object obj): myMethod(obj) {
        traceEntry("" + thisJoinPointStaticPart.getSignature(), obj);
    }
    after(Object obj): myMethod(obj) {
        traceExit("" + thisJoinPointStaticPart.getSignature(), obj);
    }
}
