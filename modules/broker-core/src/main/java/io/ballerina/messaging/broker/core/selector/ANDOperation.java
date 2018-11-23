package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;

/**
 * Implementation of a boolean expression. Here we compare two expressions and evaluate to a boolean value.
 */


public class ANDOperation implements BooleanExpression {

    private  final BooleanExpression value;
    private final BooleanExpression value1;

    public ANDOperation (BooleanExpression value2 , BooleanExpression value3) {
        this.value = value2;
        this.value1 = value3;
    }

    @Override
    public boolean evaluate (Metadata metadata) {
      Object x = value.evaluate(metadata);
        Object y = value1.evaluate(metadata);
        ConvertAndCompare con = new ConvertAndCompare();
        boolean b = con.convertToBoolean(x);
        boolean b1 = con.convertToBoolean(y);


       if (b == true && b1 == true) {
           return true;
       }

        return false;
    }
}
