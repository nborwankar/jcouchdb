package org.jcouchdb.document;

import java.util.List;
import java.util.Map;

import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;
/**
 * Interface for Documents used with jcouchdb.
 *
 * You don't actually have to implement Document, but your class needs
 * to be able to be fed both "_id" and "_rev" properties -- but without
 * those properties you cannot really work with couchdb anyway.
 *
 * @author shelmberger
 *
 */
public interface Document
{

    @JSONProperty( value = "_id", ignoreIfNull = true)
    String getId();

    void setId(String id);

    @JSONProperty( value = "_rev", ignoreIfNull = true)
    String getRevision();

    void setRevision(String revision);

    @JSONProperty( value = "_attachments", ignoreIfNull = true)
    @JSONTypeHint(Attachment.class)
    Map<String,Attachment> getAttachments();

    void setAttachments(Map<String,Attachment> attachments);

}
