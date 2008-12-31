package org.jcouchdb.db;

public interface DatabaseEventHandler
{
    void creatingDocument(Database db, Object document) throws Exception;

    void createdDocument(Database db, Object document, Response response);

    void updatingDocument(Database db, Object document) throws Exception;

    void updatedDocument(Database db, Object document, Response response);

    void deletingDocument(Database db, String id, String rev) throws Exception;

    void deletedDocument(Database db, String id, String rev, Response response);
}
