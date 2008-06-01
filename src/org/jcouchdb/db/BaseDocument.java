package org.jcouchdb.db;

import org.jcouchdb.json.AbstractDynamicAttrs;
import org.jcouchdb.json.JSONProperty;

/**
 * Convenience base class for documents. Not abstract so it can be used fully dynamically.
 *
 * @author shelmberger
 *
 */
public class BaseDocument extends AbstractDynamicAttrs implements Document
{

    private String id;
    private String revision;

    /* (non-Javadoc)
     * @see org.couchblog.db.Document#getId()
     */
    @JSONProperty("_id")
    public String getId()
    {
        return id;
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Document#setId(java.lang.String)
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Document#getRevision()
     */
    @JSONProperty(value="_rev",ignoreIfNull = true)
    public String getRevision()
    {
        return revision;
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Document#setRevision(java.lang.String)
     */
    public void setRevision(String revision)
    {
        this.revision = revision;
    }
}
