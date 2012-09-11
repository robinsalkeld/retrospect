package org.objectweb.asm;

/**
 * Silly little class that ignores the first int. The expectation is that
 * the first int is the size of the attribute and not needed.
 * @author Robin Salkeld
 */
public class AttributeContentByteVector extends ByteVector {
    private boolean first = true;

    @Override
    public ByteVector putInt(int i) {
        // Skip the first int, which is the total size of
        // the annotations and isn't included in this format.
        if (first) {
            first = false;
        } else {
            super.putInt(i);
        }
        return this;
    }
}
