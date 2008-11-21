/**
 *
 */
package org.jcouchdb.json;

import org.jcouchdb.db.NotADocument;

public class ContentBean extends NotADocument
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