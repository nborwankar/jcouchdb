/**
 *
 */
package org.jcouchdb.json;


public class DynAttrsBean
    extends AbstractDynamicAttrs
{
    private String foo;

    public String getFoo()
    {
        return foo;
    }

    public void setFoo(String foo)
    {
        this.foo = foo;
    }
}