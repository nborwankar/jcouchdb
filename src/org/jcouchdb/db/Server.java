package org.jcouchdb.db;

import java.util.List;

import org.jcouchdb.exception.CouchDBException;

/**
 * Represents a couchdb server
 *
 * @author shelmberger
 */
public interface Server
{
    public final static int DEFAULT_PORT = 5984;

    /**
     * Returns a list with all database names.
     *
     * @return
     */
    List<String> listDatabases();

    /**
     * Creates the database with the given name
     * @param name
     * @return <code>true</code> if the database could be created, <code>false</code> if they already existed
     */
    boolean createDatabase(String name) throws CouchDBException;

    /**
     * Deletes the database with the given name
     * @param name
     */
    void deleteDatabase(String name) throws CouchDBException;

    /**
     * Send a GET request to the given URI
     * @param uri
     * @return
     */
    Response get(String uri) throws CouchDBException;

    /**
     * Send a PUT request to the given URI
     * @param uri
     * @return
     */
    Response put(String uri) throws CouchDBException;

    /**
     * Send a PUT request to the given URI with
     * the given body
     * @param uri
     * @return
     */
    Response put(String uri, String body) throws CouchDBException;

    /**
     * Send a PUT request to the given URI with
     * the given byte array body
     * @param uri
     * @param contentType   content type
     * @return
     */
    Response put(String uri, byte[] body, String contentType) throws CouchDBException;

    /**
     * Send a POST request to the given URI with
     * the given body
     * @param uri
     * @return
     */
    Response post(String uri, String body) throws CouchDBException;


    /**
     * Send a DELETE request to the given URI
     *
     * @param uri
     * @return
     */
    Response delete(String uri) throws CouchDBException;

}
