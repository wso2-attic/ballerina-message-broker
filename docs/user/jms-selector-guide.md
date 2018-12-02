### JMS Selectors

This broker is written based on AMQP broker semantics. In addition, to AMQP broker semantics 
following JMS selector functionalities are supported as well.

1. __x-filter-jms-selector__ header is used to define the selector expression.
    When binding a queue to an exchange above additional header can be provided with a
    valid JMS selector expression to enable message filtering.  
2. Only the topic exchange supports JMS message selectors. 
3. Current JMS selector grammar is supported for Comparison operators, Arithmetic Operators in precedence order  
    and logical operators in precedence order with string, integer,double,exponent and float values.
   
   # Literals:
   - A string literal is enclosed in single quotes, with an included single quote represented by doubled single quote; 
   for example,
   `'literal' and 'literal''s'` 
   Like Java String literals, these use the Unicode character encoding.
   - An exact numeric literal is a numeric value without a decimal point, 
   such as 
   `57 , -957 , +62 ` 
   numbers in the range of Java long are supported.
   Exact numeric literals use the Java integer literal syntax.
   - An approximate numeric literal is a numeric value in scientific notation, 
   such as `7E3 and -57.9E2 `
   or a numeric value with a decimal, such as `7. , -95.7 , and +6.2 `
   numbers in the range of Java double are supported. Approximate literals use the Java floating-point literal syntax. 
   The boolean literals `TRUE and FALSE `.  
   
   # Identifiers:
   An identifier is an unlimited-length character sequence that must begin with a Java identifier start character;
   All following characters must be Java identifier part characters. An identifier start character is any character for which the method Character.isJavaIdentifierStart returns true .
     - This includes '_' and '$' . An identifier part character is any character for which the method Character.isJavaIdentifierPart returns true .
     - Identifiers cannot be the names NULL , TRUE , or FALSE .
     - Identifiers cannot be NOT , AND , OR , BETWEEN , LIKE , IN , IS , or ESCAPE .
     - __Identifiers are either header field references or property references.__
     - The type of a property value in a message selector corresponds to the type used to set the property. If a property that does not exist in a message is referenced, its value is NULL . 
     - The semantics of evaluating NULL values in a selector are SQL treats a NULL value as unknown. Comparison or arithmetic with an unknown value always yields an unknown value.
     - The IS NULL and IS NOT NULL operators convert an unknown header or property value into the respective TRUE and FALSE values.
     - The conversions that apply to the get methods for properties do not apply when a property is used in a message selector expression. For example, 
     - Suppose you set a property as a string value, as in the following:
       `myMessage.setStringProperty("NumberOfOrders", "2");`
     - The following expression in a message selector would evaluate to false, because a string cannot be used in an arithmetic expression:
       `"NumberOfOrders > 1"`
     - __Identifiers are case sensitive.__
     -  Message header field references are restricted to
      `JMSDeliveryMode , JMSPriority , JMSMessageID ,`
       `JMSTimestamp , JMSCorrelationID , and JMSType .`
       `JMSMessageID , JMSCorrelationID , and JMSType values` may be null and if so are treated as a NULL value . 
       
    # Expressions:
     A selector is a conditional expression; a selector that evaluates to true matches.
      - A selector that evaluates to false or unknown does not match.
        - Arithmetic expressions are composed of themselves, arithmetic operations, identifiers with numeric values, and numeric literals.
        - Conditional expressions are composed of themselves, comparison operations, logical operations, identifiers with boolean values, and boolean literals.
             __Standard bracketing () for ordering expression evaluation is supported.__
             __Logical operators in precedence order: NOT , AND , OR__
             __Comparison operators: = , > , >= , < , <= , <> (not equal)__
        - __Only like type values can be compared.__ 
        - One exception is that it is valid to compare exact numeric values and approximate numeric values (the type conversion required is defined by the rules of Java numeric promotion). 
        - If the comparison of non-like type values is attempted, the value of the operation is false . 
        - If either of the type values evaluates to NULL , the value of the expression is unknown.
        - __String and Boolean comparison is restricted to = and <> .__ Two strings are equal if and only if they contain the same sequence of characters.
        - Arithmetic operators in precedence order:
             `+ , - (unary)
             * , / (multiplication and division)
             + , - (addition and subtraction)`
        - Arithmetic operations must use Java numeric promotion.
            `arithmetic-expr1 [NOT] BETWEEN arithmetic-expr2 and arithmetic-expr3 (comparison operator)`
            `"age BETWEEN 15 AND 19" is equivalent to "age >= 15 AND age <= 19"`
            `"age NOT BETWEEN 15 AND 19" is equivalent to "age < 15 OR age > 19"`
        - __Identifier [NOT] IN (string-literal1, string- literal2,...) (comparison operator where identifier has a String or NULL value).__
            `"Country IN ('UK', 'US', 'France')" is true for 'UK' and false for 'Peru' ; it is equivalent to the expression (Country ='UK') OR (Country = 'US') OR (Country = 'France')`
             `"Country NOT IN ('UK', 'US', 'France')" is false for 'UK'and true for 'Peru'; it is equivalent to the expression "NOT ((Country = 'UK') OR (Country = 'US') OR (Country = 'France'))"`
        - If identifier of an IN or NOT IN operation is NULL, the value of the operation is unknown.
        - Identifier [NOT] LIKE pattern-value [ESCAPE escape- character] (comparison operator, 
        - where identifier has a String value; pattern-value is a string literal where '_' stands for any single character; '%' stands for any sequence of characters, including the empty sequence, and all other characters stand for themselves. 
        - The optional escape-character is a single-character string literal whose character is used to escape the special meaning of the '_' and '%' in pattern-value.)
            `"phone LIKE '12%3'" is true for '123' or '12993' and false for '1234'
            "word LIKE 'l_se'" is true for 'lose' and false for 'loose'
            "underscored LIKE '\_%' ESCAPE '\'" is true for '_foo' and false for 'bar'
            "phone NOT LIKE '12%3'" is false for '123' and '12993' and true for '1234'`
        - If identifier of a LIKE or NOT LIKE operation is NULL , the value of the operation is unknown.
        - Identifier IS NULL (comparison operator that tests for a null header field value or a missing property value)
            ` "prop_name IS NULL"`
        - Identifier IS NOT NULL (comparison operator that tests for the existence of a non-null header field value or property value)
            `"prop_name IS NOT NULL"`
        - JMS providers are required to verify the syntactic correctness of a message selector at the time it is presented. A method providing a syntactically incorrect selector must result in a JMS InvalidSelectorException .
        - JMS providers may also optionally provide some semantic checking at the a time the selector is presented. Not all semantic checking can be performed at the time a message selector is presented, because property types are not known.
        - The following message selector selects messages with a message type of car and color of blue and weight greater than 2500 lbs:
            `"JMSType = 'car' AND color = 'blue' AND weight > 2500"`

    
 
