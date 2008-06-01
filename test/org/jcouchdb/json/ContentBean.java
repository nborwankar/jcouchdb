/**
 *
 */
package org.jcouchdb.json;

import org.jcouchdb.db.BaseDocument;

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