package org.jcouchdb.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jcouchdb.json.JSONParser;

public class Response
{
    protected static Logger log = Logger.getLogger(Response.class);

    private int code;
    private String content;

    private JSONParser parser;

    public Response(int code, String content)
    {
        this.code = code;
        this.content = content;

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

    public String getContent()
    {
        return content;
    }

    public List getContentAsList()
    {
        return getParser().parse(ArrayList.class, content);
    }


    public Map getContentAsMap()
    {
        return getParser().parse(HashMap.class, content);
    }

    public <T> T getContentAsBean(Class<T> cls)
    {
        return getParser().parse(cls, content);
    }

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

