package org.jcouchdb.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.jcouchdb.exception.CouchDBException;
import org.jcouchdb.exception.NoResponseException;
import org.jcouchdb.util.Assert;
import org.jcouchdb.util.ExceptionWrapper;

/**
 * Default implementation of the {@link Server} interface.
 * 
 * @author shelmberger
 */
public class ServerImpl
    implements Server
{
    private static final String CHARSET = "UTF-8";

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
        httpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
    }

    /**
     * {@inheritDoc}
     */
    public List<String> listDatabases()
    {
        Response resp = null;
        try
        {
            resp = get("/_all_dbs");
            if (!resp.isOk())
            {
                throw new CouchDBException("Error listing databases: " + resp);
            }
            return resp.getContentAsList();
        }
        finally
        {
            if (resp != null)
            {
                resp.destroy();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean createDatabase(String name)
    {
        Response resp = null;
        try
        {
            resp = put("/" + name + "/");
            if (resp.isOk())
            {
                return true;
            }
            else
            {
                if (resp.getCode() == 412 || resp.getCode() == 500)
                {
                    return false;
                }
                else
                {
                    throw new CouchDBException("Error creating database: " + resp);
                }
            }
        }
        finally
        {
            if (resp != null)
            {
                resp.destroy();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteDatabase(String name)
    {
        Response resp = null;
        try
        {
            resp = delete("/" + name + "/");
            if (!resp.isOk())
            {
                throw new CouchDBException("Cannot delete database " + name + ": " + resp);
            }
        }
        finally
        {
            if (resp != null)
            {
                resp.destroy();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Response get(String uri)
    {
        if (log.isDebugEnabled())
        {
            log.debug("GET " + uri);
        }

        GetMethod method = new GetMethod(uri);

        try
        {
            int code = httpClient.executeMethod(method);
            return new Response(code, method);
        }
        catch (NoHttpResponseException e)
        {
            throw new NoResponseException();
        }
        catch (HttpException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IOException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (Exception e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Response put(String uri)
    {
        return put(uri, null);
    }

    /**
     * {@inheritDoc}
     */
    public Response put(String uri, String body)
    {
        if (log.isDebugEnabled())
        {
            log.debug("PUT " + uri + ", body = " + body);
        }
        PutMethod putMethod = new PutMethod(uri);
        if (body != null)
        {
            try
            {
                putMethod.setRequestEntity(new StringRequestEntity(body, "application/json",
                    CHARSET));
            }
            catch (UnsupportedEncodingException e)
            {
                throw ExceptionWrapper.wrap(e);
            }
        }

        return executePutMethod(putMethod);
    }

    /**
     * {@inheritDoc}
     */
    public Response put(String uri, byte[] body, String contentType)
    {
        if (log.isDebugEnabled())
        {
            log.debug("PUT " + uri + ", body = " + body);
        }

        PutMethod putMethod = new PutMethod(uri);
        if (body != null)
        {
            putMethod.setRequestEntity(new ByteArrayRequestEntity(body, contentType));
        }

        return executePutMethod(putMethod);
    }

    public Response put(String uri, InputStream inputStream, String contentType) throws CouchDBException
    {
        Assert.notNull(inputStream, "inputStream can't be null");
        
        if (log.isDebugEnabled())
        {
            log.debug("PUT " + uri + ", inputStream = " + inputStream);
        }

        PutMethod putMethod = new PutMethod(uri);
        putMethod.setRequestEntity(new InputStreamRequestEntity(inputStream, contentType));
        return executePutMethod(putMethod);
    }
    
    private Response executePutMethod(PutMethod putMethod)
    {
        try
        {
            int code = httpClient.executeMethod(putMethod);
            return new Response(code, putMethod);
        }
        catch (NoHttpResponseException e)
        {
            throw new NoResponseException();
        }
        catch (HttpException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IOException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (Exception e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Response post(String uri, String body)
    {
        if (log.isDebugEnabled())
        {
            log.debug("POST " + uri + ", body = " + body);
        }

        PostMethod postMethod = new PostMethod(uri);

        try
        {
            postMethod.setRequestEntity(new StringRequestEntity(body, "application/json", CHARSET));
            int code = httpClient.executeMethod(postMethod);
            Response response = new Response(code, postMethod);

            return response;
        }
        catch (NoHttpResponseException e)
        {
            throw new NoResponseException();
        }
        catch (HttpException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IOException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (Exception e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Response delete(String uri)
    {
        if (log.isDebugEnabled())
        {
            log.debug("DELETE " + uri);
        }

        DeleteMethod deleteMethod = new DeleteMethod(uri);

        Response responce = null;
        try
        {
            int code = httpClient.executeMethod(deleteMethod);
            Response response = new Response(code, deleteMethod);

            return response;
        }
        catch (NoHttpResponseException e)
        {
            throw new NoResponseException();
        }
        catch (HttpException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IOException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (Exception e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setCredentials(AuthScope authScope, Credentials credentials)
    {
        httpClient.getState().setCredentials(authScope, credentials);
    }
}
