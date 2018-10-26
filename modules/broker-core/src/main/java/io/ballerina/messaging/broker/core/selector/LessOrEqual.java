package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;

/**
 * Implementation of a boolean expression. Here we compare two expressions and evaluate to a boolean value.
 */

public class LessOrEqual implements BooleanExpression {

    private final Expression<Metadata> left;

    private final Expression<Metadata> right;

    public LessOrEqual (Expression<Metadata> left , Expression<Metadata> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean evaluate(Metadata metadata) {
        Object leftValue = left.evaluate(metadata);
        Object rightValue = right.evaluate(metadata);
        if (leftValue == null || rightValue == null) {
            return false;
        }
        String s = String.valueOf(leftValue);
        String s1 = String.valueOf(rightValue);
        double x = s.compareTo(s1);
        if (x == 0 || x == -1) {
            return true;
        }

        return false;
    }
}
