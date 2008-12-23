package org.jcouchdb.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jcouchdb.document.InstanceCachable;
import org.svenson.JSONParser;

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
    private byte[] content;

    private JSONParser parser;

    private Map<Class,Object> responseCache = new HashMap<Class,Object>();

    private String stringContent;

    public Response(int code, byte[] content)
    {
        this.code = code;
        this.content = content;

        if (log.isDebugEnabled())
        {
            log.debug(this);
        }
    }

    public Response(int code, String content)
    {
        this.code = code;
        this.content = null;
        this.stringContent = content;

        if (log.isDebugEnabled())
        {
            log.debug(this);
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
        if (content == null)
        {
            content = stringContent.getBytes();
        }

        return content;
    }

    public String getContentAsString()
    {
        if (stringContent == null)
        {
            stringContent = new String(content);
            content = null;
        }
        return stringContent;
    }

    /**
     * Returns the contents of the response as List
     * @return
     */
    public List getContentAsList()
    {
        return getParser().parse(List.class, getContentAsString());
    }

    /**
     * Returns the contents of the response as Map
     * @return
     */
    public Map getContentAsMap()
    {
        return getParser().parse(Map.class, getContentAsString());
    }

    /**
     * Returns the contents of the response as bean of the
     * given type.
     *
     * @return
     */
    public <T> T getContentAsBean(Class<T> cls)
    {
        T cached = null;

        boolean instanceCachable = cls.getAnnotation(InstanceCachable.class) != null;

        if (instanceCachable)
        {
            cached = (T)responseCache.get(cls);
        }
        if (cached == null)
        {
            cached = getParser().parse(cls, getContentAsString());

            if (instanceCachable)
            {
                responseCache.put(cls, cached);
            }
        }
        return cached;
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
        return super.toString()+": code = "+code+", content = "+( stringContent != null ? stringContent : new String(content));
    }
}

