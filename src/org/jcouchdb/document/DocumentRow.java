package org.jcouchdb.document;

import org.svenson.JSONProperty;

/**
 * One row of a view result.
 *
 * @author shelmberger
 *
 * @param <V> Type of the value wrapped by this view result row.
 * @param <D> document type of the value wrapped by this view result row.
 */
public class DocumentRow<V,D>
    extends ValueRow<V>
{
    private D document;

    @JSONProperty("doc")
    public D getDocument()
    {
        return document;
    }

    public void setDocument(D document)
    {
        this.document = document;
    }
}
