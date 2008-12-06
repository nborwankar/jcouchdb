package org.jcouchdb.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
        this.content = content.getBytes();

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
        return content;
    }

    public String getContentAsString()
    {
        return new String(content);
    }

    /**
     * Returns the contents of the response as List
     * @return
     */
    public List getContentAsList()
    {
        return getParser().parse(ArrayList.class, getContentAsString());
    }

    /**
     * Returns the contents of the response as Map
     * @return
     */
    public Map getContentAsMap()
    {
        return getParser().parse(HashMap.class, getContentAsString());
    }

    /**
     * Returns the contents of the response as bean of the
     * given type.
     *
     * @return
     */
    public <T> T getContentAsBean(Class<T> cls)
    {
        return getParser().parse(cls, getContentAsString());
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
        return super.toString()+": code = "+code+", content = "+content;
    }
}

