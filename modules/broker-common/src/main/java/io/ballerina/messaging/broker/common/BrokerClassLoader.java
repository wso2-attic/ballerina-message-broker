package io.ballerina.messaging.broker.common;

/**
 * Util class used for loading classes.
 */
public class BrokerClassLoader {

    /**
     * Return a new object from the given class name.
     *
     * @param className       full qualified class name
     * @param returnClassName class of the returned object
     * @return new Object of the given class name
     * @throws ClassNotFoundException if class is not found in the class loader
     * @throws IllegalAccessException if the object constructor is not accessible
     * @throws InstantiationException if the object cannot be initialized
     */
    public static <T> T loadClass(String className, Class<T> returnClassName)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Object object = Class.forName(className).newInstance();
        return returnClassName.cast(object);
    }
}
