/**
 *
 */
package org.jcouchdb.json;

import org.jcouchdb.document.BaseDocument;

public class ContentBean extends BaseDocument
{
    private String value;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}