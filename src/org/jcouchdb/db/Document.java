package org.jcouchdb.db;

import org.jcouchdb.json.JSONProperty;

public interface Document
{

    @JSONProperty("_id")
    String getId();

    void setId(String id);

    @JSONProperty("_rev")
    String getRevision();

    void setRevision(String revision);

}
