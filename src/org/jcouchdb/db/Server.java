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

    List<String> listDatabases();

    void createDatabase(String name);

    void deleteDatabase(String name);

    Response get(String uri);

    Response put(String uri);

    Response put(String uri, String body);

    Response post(String uri, String body);

    Response delete(String uri);

}
