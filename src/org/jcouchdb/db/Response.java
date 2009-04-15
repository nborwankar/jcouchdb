package org.jcouchdb.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jcouchdb.exception.DataAccessException;
import org.jcouchdb.util.Assert;
import org.jcouchdb.util.ExceptionWrapper;
import org.svenson.JSONParser;
import org.svenson.tokenize.InputStreamSource;

import com.sun.org.apache.bcel.internal.ExceptionConstants;

/**
 * Encapsulates a couchdb server response with error code and received
 * body.
 *
 * @author shelmberger
 *
 */
public class Response
{
    protected static Logger log = Logger.getLogger(Response.class);

    private int code;

    private JSONParser parser;

    private Header[] headers;

    private InputStream inputStream;

    private HttpMethodBase method;

    public Response(int code, String s)
    {
        this( code, new ByteArrayInputStream(s.getBytes()), null);
    }

    public Response(int code, InputStream stream, int length)
    {
        this( code, stream, null);
    }

    public Response(int code, HttpMethodBase method) throws IOException
    {
        this( code, method.getResponseBodyAsStream(), method.getResponseHeaders());
        this.method = method;        
    }

    public Response(int code, InputStream stream, Header[] headers)
    {
        Assert.notNull(stream, "stream can't be null");
        
        this.inputStream = stream;
        this.code = code;
        this.headers = headers;

        if (log.isTraceEnabled())
        {
            log.trace(this);
        }
    }

    public void setParser(JSONParser parser)
    {
        this.parser = parser;
    }

    private JSONParser getParser()
    {
        if (parser == null)
        {
            parser = new JSONParser();
        }
        return parser;
    }

    public int getCode()
    {
        return code;
    }
    

    public byte[] getContent()
    {        
        try
        {
            return IOUtils.toByteArray(inputStream);
        }
        catch (IOException e)
        {
            throw new DataAccessException("error reading content from response", null);
        }
    }

    public String getContentAsString()
    {
        if (log.isDebugEnabled())
        {
            log.debug("getContentAsString on "+this);
        }
        return new String(getContent());
    }

    /**
     * Returns the contents of the response as List
     * @return
     */
    public List getContentAsList()
    {
        List list = getParser().parse(List.class, new InputStreamSource(inputStream, false));
        return list;
    }

    /**
     * Returns the contents of the response as Map
     * @return
     */
    public Map getContentAsMap()
    {
        Map map = getParser().parse(Map.class, new InputStreamSource(inputStream, false));
        return map;
    }

    /**
     * Returns the contents of the response as bean of the
     * given type.
     *
     * @return
     */
    public <T> T getContentAsBean(Class<T> cls)
    {
        T t = getParser().parse(cls, new InputStreamSource(inputStream, false));
        return t;
    }

    public Header[] getResponseHeaders()
    {
        return headers;
    }
    
    public InputStream getInputStream()
    {
        return inputStream;
    }

    /**
     * Returns <code>true</code> if the response code is
     * between 200 and 299
     * @return
     */
    public boolean isOk()
    {
        return code >= 200 && code <= 299;
    }

    @Override
    public String toString()
    {
        return super.toString()+": code = "+code+", stream = " + inputStream;
    }

    public void destroy()
    {
        if (method != null)
        {
            method.releaseConnection();
            inputStream = null;
            method = null; 
        }
    }
}

