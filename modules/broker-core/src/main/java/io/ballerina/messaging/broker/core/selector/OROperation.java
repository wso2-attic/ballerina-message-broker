package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;

/**
 * Implementation of a boolean expression. Here we compare two expressions and evaluate to a boolean value.
 */

public class OROperation implements BooleanExpression {

    private final BooleanExpression value;
    private final BooleanExpression value1;

    public OROperation (BooleanExpression value , BooleanExpression value1) {
        this.value = value;
        this.value1 = value1;
    }

    @Override
    public boolean evaluate (Metadata metadata) {
        Object x = value.evaluate(metadata);
        Object y = value1.evaluate(metadata);
       ConvertAndCompare con = new ConvertAndCompare();
        boolean b = con.convertToBoolean(x);
        boolean b1 = con.convertToBoolean(y);

        if (b == true || b1 == true) {
            return true;
        }

        return false;
    }
}
