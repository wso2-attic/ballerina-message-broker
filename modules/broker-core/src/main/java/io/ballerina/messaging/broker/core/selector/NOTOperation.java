package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;

/**
 * Implementation of a boolean expression. Here we compare two expressions and evaluate to a boolean value.
 */

public class NOTOperation implements BooleanExpression {

    private  final BooleanExpression value;

    public NOTOperation (BooleanExpression value) {
        this.value = value;
    }

    @Override
    public boolean evaluate (Metadata metadata) {

        Object x = value.evaluate(metadata);

        ConvertAndCompare con = new ConvertAndCompare();
        boolean b = con.convertToBoolean(x);

        if (b == true) {
            return false;
        }
        return true;
    }
}
