package org.jcouchdb.db;

import java.io.*;
import java.util.List;


import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.HttpVersion;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.methods.*;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.AuthScope;
import org.jcouchdb.exception.CouchDBException;
import org.jcouchdb.exception.NoResponseException;
import org.jcouchdb.util.Assert;
import org.jcouchdb.util.ExceptionWrapper;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Default implementation of the {@link Server} interface.
 * 
 * @author shelmberger
 */
public class ServerImpl
    implements Server
{
    private static final String CHARSET = "UTF-8";

    protected static Logger             log = LoggerFactory.getLogger(ServerImpl.class);

    private HttpParams                  defaultParameters;
    private SchemeRegistry              supportedSchemes;
    private ClientConnectionManager     clcm;
    private AuthScope                   authScope;
    private Credentials                 credentials;
    private HttpContext                 context;

    private String                      serverURI;
    private DefaultHttpClient           httpClient;

    private int                         MAX_CONNECTIONS_PER_ROUTE;
    private int                         MAX_TOTAL_CONNECTIONS;


    {

        MAX_CONNECTIONS_PER_ROUTE   = 100;
        MAX_TOTAL_CONNECTIONS       = 1000;

        setup();
        clcm = createManager();
        httpClient = getHttpClient();
    }

    private final void setup() {
        supportedSchemes = new SchemeRegistry();
        SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));

        HttpParams params = new BasicHttpParams( );
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUseExpectContinue(params, false);

        defaultParameters = params;

        context = new BasicHttpContext( );
    }

    private final ClientConnectionManager createManager() {
        defaultParameters.setParameter(
                ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
                new ConnPerRouteBean( MAX_CONNECTIONS_PER_ROUTE )
        );

        defaultParameters.setParameter(
                ConnManagerPNames.MAX_TOTAL_CONNECTIONS,
                MAX_TOTAL_CONNECTIONS
        );

        return new ThreadSafeClientConnManager( defaultParameters, supportedSchemes );
    }

    private final DefaultHttpClient getHttpClient(){
        DefaultHttpClient httpClient = new DefaultHttpClient( clcm, defaultParameters );
        if( authScope != null ){
            httpClient.getCredentialsProvider().setCredentials( authScope, credentials );
        }
        return httpClient;
    }

    private final Response execute( HttpRequestBase request ) throws IOException {
        HttpResponse res = httpClient.execute( request, context );
        Response response = new Response( res );
        request.abort();
        return response;
    }

    private Response executePut(HttpPut put) {
        try {
            return execute( put );
        }
        catch (IOException e) {
            put.abort();
            throw ExceptionWrapper.wrap(e);
        }
    }

    

    public ServerImpl(String host)
    {
        this(host, DEFAULT_PORT);
    }

    public ServerImpl(String host, int port)
    {
        this.serverURI = "http://" + host + ":" + port;
        /*this.hostConfiguration = new HostConfiguration();
        this.hostConfiguration.setHost(host, port);
        httpClient.setHostConfiguration(hostConfiguration);
        httpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());*/
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

        HttpGet get = new HttpGet( serverURI + uri );
        
        try
        {
            return execute( get );
        }
        catch (IOException e) {
            get.abort();
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
        HttpPut put = new HttpPut( serverURI + uri );
        if (body != null) {
            try {
                StringEntity reqEntity = new StringEntity( body );
                reqEntity.setContentType("application/json");
                reqEntity.setContentEncoding( CHARSET );
                put.setEntity( reqEntity );
            }
            catch (UnsupportedEncodingException e) {
                throw ExceptionWrapper.wrap(e);
            }
        }

        return executePut( put );        
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
        
        HttpPut put = new HttpPut( serverURI + uri );
        if (body != null) {
            ByteArrayEntity reqEntity = new ByteArrayEntity( body );
            reqEntity.setContentType( contentType );
            put.setEntity( reqEntity );
        }

        return executePut( put );
    }

    private byte[] getAsByteArray( InputStream inputStream ) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        BufferedInputStream bin = new BufferedInputStream( inputStream );
        int i = 0;
        while( (i = bin.read()) != -1 )
            bout.write( i );
        bin.close();
        bout.close();
        return bout.toByteArray();
    }
    
    public Response put(String uri, InputStream inputStream, String contentType) throws CouchDBException
    {
        Assert.notNull(inputStream, "inputStream can't be null");

        if (log.isDebugEnabled())
        {
            log.debug("PUT " + uri + ", inputStream = " + inputStream);
        }

        try {
            return put( uri, getAsByteArray( inputStream ), contentType );
        } catch (IOException e) {
            throw new CouchDBException( e );
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

        HttpPost post = new HttpPost( serverURI + uri );

        try {
            StringEntity reqEntity = new StringEntity( body );
            reqEntity.setContentType("application/json");
            reqEntity.setContentEncoding( CHARSET );
            post.setEntity( reqEntity );

            return execute( post );
        }
        catch (IOException e) {
            post.abort();
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

        HttpDelete delete = new HttpDelete( serverURI + uri );

        try {
            return execute( delete );
        }
        catch (IOException e) {
            delete.abort();
            throw ExceptionWrapper.wrap(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setCredentials(AuthScope authScope, Credentials credentials)
    {
        this.authScope      = authScope;
        this.credentials    = credentials;
        httpClient = getHttpClient();
    }
}
