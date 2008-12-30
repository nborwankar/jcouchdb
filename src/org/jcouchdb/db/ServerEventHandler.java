package org.jcouchdb.db;

public interface ServerEventHandler
{
    void executing(String method, String uri, Object data) throws Exception;

    void executed(String method, String uri, Object data, Response response);
}
