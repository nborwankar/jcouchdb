package org.jcouchdb.db;

import java.util.List;

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
     */
    void createDatabase(String name);

    /**
     * Deletes the database with the given name
     * @param name
     */
    void deleteDatabase(String name);

    /**
     * Send a GET request to the given URI
     * @param uri
     * @return
     */
    Response get(String uri);

    /**
     * Send a PUT request to the given URI
     * @param uri
     * @return
     */
    Response put(String uri);

    /**
     * Send a PUT request to the given URI with
     * the given body
     * @param uri
     * @return
     */
    Response put(String uri, String body);

    /**
     * Send a POST request to the given URI with
     * the given body
     * @param uri
     * @return
     */
    Response post(String uri, String body);

    /**
     * Send a DELETE request to the given URI
     *
     * @param uri
     * @return
     */
    Response delete(String uri);

}
