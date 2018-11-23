package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;

/**
 * Implementation of a convert objects and comparing. Here we compare two objects and evaluate to a double value.
 */

public class NotBetweenComparision implements BooleanExpression {

    private final Expression<Metadata> left;

    private final Expression<Metadata> ltr1;

    private final Expression<Metadata> ltr2;


    public NotBetweenComparision (Expression<Metadata> left , Expression<Metadata> ltr1 , Expression<Metadata> ltr2) {
        this.left = left;
        this.ltr2 = ltr2;
        this.ltr1 = ltr1;

    }

    @Override
    public boolean evaluate (Metadata metadata) {
        Object leftValue = left.evaluate(metadata);
        Object ltr1Value = ltr1.evaluate(metadata);
        Object ltr2Value = ltr2.evaluate(metadata);
        if (leftValue == null) {
            return false;
        }
        String s = String.valueOf(leftValue);
        String s1 = String.valueOf(ltr1Value);
        String s2 = String.valueOf(ltr2Value);
        double b = Double.valueOf(s);
        double b1 = Double.valueOf(s1);
        double b2 = Double.valueOf(s2);
        if (((leftValue == ltr1Value) || (b > b1)) && ((leftValue == ltr2Value)  || (b < b2))) {

            return false;
        }


        return true;
    }

}
