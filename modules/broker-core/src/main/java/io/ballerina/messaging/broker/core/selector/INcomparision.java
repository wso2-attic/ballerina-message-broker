package io.ballerina.messaging.broker.core.selector;

import io.ballerina.messaging.broker.core.Metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of a convert objects and comparing. Here we compare two objects and evaluate to a double value.
 */
public class INcomparision implements BooleanExpression {

    private final Expression<Metadata> left;
     private  final List elements;


    public INcomparision (Expression<Metadata> left , List elements) {
        this.left = left;
        this.elements = elements;
    }

    @Override
    public boolean evaluate (Metadata metadata) {
        Collection t = null;
        Object rvalue = left.evaluate(metadata);
        if (elements.size() == 0) {
            t = null;
          }

         if (elements.size() < 5) {
            t = elements;
           }

        if (elements.size() > 5) {
            t = new HashSet(elements);
        }

        final Collection inList = t;


        if (rvalue == null) {
            return false;
        }

        if (rvalue.getClass() != String.class) {
            return false;
        }

        if (((inList != null) && inList.contains(rvalue))) {
            return true;
        }


        return false;
    }
}
