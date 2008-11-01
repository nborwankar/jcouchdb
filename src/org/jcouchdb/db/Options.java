package org.jcouchdb.db;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.svenson.JSON;

/**
 * Used to pass query options to view queries.
 * For example:
 * <pre>
 * database.queryView("company/all", Map.class, new Options().count(1).descending(true);
 * </pre>
 *
 * @see Database#queryView(String, Class, Options)
 * @see Database#queryAdHocView(Class, Options)
 * @author shelmberger
 *
 */
public class Options extends HashMap<String, Object>
{
    private static final long serialVersionUID = -4025495141211906568L;

    private JSON optionsJSON = new JSON();

    public Options()
    {
    }

    public Options(String key, Object value)
    {
        put(key, value);
    }

    @Override
    public Options put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }

    public Options key(String key)
    {
        return put("key",key);
    }
    public Options startKey(String key)
    {
        return put("startkey",key);
    }

    public Options startKeyDocId(String docId)
    {
        return put("startkey_docid", docId);
    }

    public Options endKey(String key)
    {
        return put("endkey",key);
    }

    public Options count(int count)
    {
        return put("count", count);
    }

    public Options update(boolean update)
    {
        return put("update",update);
    }

    public Options descending(boolean update)
    {
        return put("descending",update);
    }

    public Options skip(int skip)
    {
        return put("skip",skip);
    }

    public String toQuery()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("?");

        boolean first = true;
        try
        {
            for (Map.Entry<String, Object> e : entrySet())
            {
                if (!first)
                {
                    sb.append("&");
                }
                sb.append(e.getKey()).append("=");
                String json = optionsJSON.forValue(e.getValue());

                sb.append(URLEncoder.encode(json, "UTF-8"));
                first = false;
            }
            if (sb.length() <= 1)
            {
                return "";
            }
            else
            {
                return sb.toString();
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new CouchDBException("error converting option value", e);
        }
    }

    /**
     * Can be imported statically to have a syntax a la <code>option().count(1);</code>.
     * @return new Option instance
     */
    public static Options option()
    {
        return new Options();
    }
}
