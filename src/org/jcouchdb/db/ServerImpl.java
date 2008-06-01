package org.jcouchdb.db;

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.jcouchdb.util.ExceptionWrapper;

/**
 * Default implementation of the {@link Server} interface.
 *
 * @author shelmberger
 *
 */
public class ServerImpl implements Server
{
    protected static Logger log = Logger.getLogger(ServerImpl.class);

    private HostConfiguration hostConfiguration;

    private HttpClient httpClient = new HttpClient();

    public ServerImpl(String host)
    {
        this(host, DEFAULT_PORT);
    }

    public ServerImpl(String host, int port)
    {
        this.hostConfiguration = new HostConfiguration();
        this.hostConfiguration.setHost(host, port);
        httpClient.setHostConfiguration(hostConfiguration);
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Server#listDatabases()
     */
    public List<String> listDatabases()
    {
        Response resp = get("/_all_dbs");
        if (!resp.isOk())
        {
            throw new CouchDBException("Error listing databases: "+resp);
        }
        return resp.getContentAsList();
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Server#createDatabase(java.lang.String)
     */
    public void createDatabase(String name)
    {
        Response resp = put("/"+name+"/");
        if (!resp.isOk())
        {
            if (resp.getCode() == 409)
            {
                throw new CouchDBException("Database already exists");
            }
            else
            {
                throw new CouchDBException("Error creating database: "+resp);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Server#deleteDatabase(java.lang.String)
     */
    public void deleteDatabase(String name)
    {
        Response resp = delete("/"+name+"/");
        if (!resp.isOk())
        {
            throw new CouchDBException("Cannot delete database "+name+": "+resp);
        }
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Server#get(java.lang.String)
     */
    public Response get(String uri)
    {
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("GET "+uri);
            }

            GetMethod method = new GetMethod(uri);
            int code = httpClient.executeMethod(method);
            return new Response(code, method.getResponseBodyAsString());
        }
        catch (HttpException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IOException e)
        {
            throw ExceptionWrapper.wrap(e);
        }

    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Server#put(java.lang.String)
     */
    public Response put(String uri)
    {
        return put(uri,null);
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Server#put(java.lang.String, java.lang.String)
     */
    public Response put(String uri, String body)
    {
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("PUT "+uri+", body = "+body);
            }
            PutMethod putMethod = new PutMethod(uri);
            if (body != null)
            {
                putMethod.setRequestEntity(new StringRequestEntity(body, "application/json", "UTF-8"));
            }
            int code = httpClient.executeMethod(putMethod);
            return new Response(code, putMethod.getResponseBodyAsString());
        }
        catch (HttpException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IOException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Server#post(java.lang.String, java.lang.String)
     */
    public Response post(String uri, String body)
    {
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("POST "+uri+", body = "+body);
            }
            PostMethod postMethod = new PostMethod(uri);
            postMethod.setRequestEntity(new StringRequestEntity(body, "application/json", "UTF-8"));
            int code = httpClient.executeMethod(postMethod);
            return new Response(code, postMethod.getResponseBodyAsString());
        }
        catch (HttpException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IOException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }

    /* (non-Javadoc)
     * @see org.couchblog.db.Server#delete(java.lang.String)
     */
    public Response delete(String uri)
    {
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("DELETE "+uri);
            }

            DeleteMethod deleteMethod = new DeleteMethod(uri);
            int code = httpClient.executeMethod(deleteMethod);
            return new Response(code, deleteMethod.getResponseBodyAsString());
        }
        catch (HttpException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IOException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }
}
