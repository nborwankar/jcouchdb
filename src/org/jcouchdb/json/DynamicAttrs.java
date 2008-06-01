package org.jcouchdb.json;

import java.util.Set;

/**
 * Interface that allows classes to also have dynamic attributes
 *
 * @author shelmberger
 *
 */
public interface DynamicAttrs
{
    /**
     * Sets the attribute with the given name to the given value.
     *
     * @param name
     * @param value if <code>null</code>, the attribute is removed.
     */
    void setAttribute(String name, Object value);

    /**
     * returns value of the attribute with the given name.
     *
     * @param name
     */
    Object getAttribute(String name);

    /**
     * Returns the set of available attribute names.
     *
     * @return
     */
    Set<String> attributeNames();
}
