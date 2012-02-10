package examples;

@Regions({"This", "Output"})
public class PrintStream {

    @Reads("This") @Writes({"This", "Output"}) 
    public void println(String x) {
        // ...
    }
    
    // ...
}
