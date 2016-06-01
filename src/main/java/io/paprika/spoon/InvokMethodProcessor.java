package io.paprika.spoon;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.support.reflect.declaration.CtMethodImpl;

/**
 * Reports warnings when empty catch blocks are found.
 */
public class InvokMethodProcessor extends AbstractProcessor<CtMethodImpl> {

    @Override
    public void process(CtMethodImpl invok) {
        System.out.println(invok.getReference().toString());
    }
}
