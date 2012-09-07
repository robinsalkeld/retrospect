package edu.ubc.retrospect;

import java.io.Reader;
import java.io.StringReader;

import java_cup.runtime.Symbol;
import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.util.SilentErrorQueue;
import abc.aspectj.ast.AJNodeFactory_c;
import abc.aspectj.ast.Pointcut;
import abc.aspectj.parse.Grm;
import abc.aspectj.parse.Lexer_c;
import abc.aspectj.types.AJTypeSystem_c;
import abc.main.CompilerAbortedException;

public class PointcutParser {

    private final SilentErrorQueue eq = new SilentErrorQueue(0, "pointcut parsing");
    private final TypeSystem ts = new AJTypeSystem_c();
    private final NodeFactory nf = new AJNodeFactory_c();
    
    public PointcutParser() {
        // Has to be called for the side-effect of setting the AbcExtension,
        // since the Lexer_c constructor reads it. Grr.
        try {
            new abc.main.Main(new String[] { "foo" } );
        } catch (CompilerAbortedException e) {
            // Don't care.
        }
    }
    
    public Pointcut parse(String expr) {
        Reader reader = new StringReader(expr);
        
        Lexer_c lexer = new Lexer_c(reader, "<runtime expression>", eq);
        lexer.enterLexerState(lexer.pointcut_state());
        
        Grm grm = new Grm(lexer, ts, nf, eq);
        Symbol result;
        try {
            result = grm.parsePointcut();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // No point since we can't reuse it.
        // lexer.returnToPrevState();
        
        return (Pointcut)result.value;
    }
    
}
