package edu.ubc.retrospect;

import java.io.IOException;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeMunger;

/**
 * A type munger that doesn't actually munge anything.
 * 
 * @author robinsalkeld
 */
public class NoopTypeMunger extends ResolvedTypeMunger {

    public NoopTypeMunger(Kind kind, ResolvedMember signature) {
        super(kind, signature);
    }

    @Override
    public void write(CompressingDataOutputStream s) throws IOException {
    }
}
