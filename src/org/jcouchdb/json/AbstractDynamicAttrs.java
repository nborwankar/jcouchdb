package org.jcouchdb.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for bean that want to use dynamic properties.
 *
 * @author shelmberger
 */
public abstract class AbstractDynamicAttrs implements DynamicAttrs
{
    private Map<String,Object> attrs = new HashMap<String, Object>();

    public Object getAttribute(String name)
    {
        return attrs.get(name);
    }

    public void setAttribute(String name, Object value)
    {
        attrs.put(name,value);
    }

    public Set<String> attributeNames()
    {
        return attrs.keySet();
    }
}
