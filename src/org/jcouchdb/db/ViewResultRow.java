package org.jcouchdb.db;

import org.jcouchdb.json.AbstractDynamicAttrs;

/**
 * One row of a view result.
 *
 * @author shelmberger
 *
 * @param <T> Type of the value wrapped by this {@link ViewResultRow}
 */
public class ViewResultRow<T>
    extends AbstractDynamicAttrs
{
    private String id;

    private Object key;

    private T value;

    /**
     * Returns the id of the result object
     *
     * @return
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the id of the result object
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Returns the key this object was mapped to
     * @return
     */
    public Object getKey()
    {
        return key;
    }

    public void setKey(Object key)
    {
        this.key = key;
    }

    /**
     * @return
     */
    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }
}
