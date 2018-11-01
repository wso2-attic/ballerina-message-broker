package io.ballerina.messaging.broker.core.selector;
/**
 * Implementation of a convert objects and comparing. Here we compare two objects and evaluate to a double value.
 */
public class ConvertAndCompare {

    public double cnovert (Object x , Object y) {
        String s = String.valueOf(x);
        String s1 = String.valueOf(y);
        double value = s.compareTo(s1);
        return value;
    }
}
