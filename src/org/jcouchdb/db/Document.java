package org.jcouchdb.db;

import org.jcouchdb.json.JSONProperty;

public interface Document
{

    @JSONProperty( value = "_id", ignoreIfNull = true)
    String getId();

    void setId(String id);

    @JSONProperty( value = "_rev", ignoreIfNull = true)
    String getRevision();

    void setRevision(String revision);

}
