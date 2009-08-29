package org.jcouchdb.db;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jcouchdb.exception.CouchDBException;
import org.svenson.JSON;
import org.svenson.JSONParser;

/**
 * Used to pass query options to view queries.
 * For example:
 * <pre>
 * database.queryView("company/all", Map.class, new Options().count(1).descending(true);
 * </pre>
 *
 * @see Database#getDocument(Class, String)
 * @see Database#queryView(String, Class, Options, JSONParser)
 * @see Database#queryAdHocView(Class, String, Options, JSONParser)
 * @author shelmberger
 *
 */
public class Options
{
    private static final long serialVersionUID = -4025495141211906568L;

    private JSON optionsJSON = new JSON();

    private Map<String, Object> content = new HashMap<String, Object>();
    
    public Options()
    {

    }

    public Options(Map<String,String> map)
    {
        content.putAll(map);
    }

    /**
     * Copies the options of the given Options object if it is not <code>null</code>.
     *
     * @param options   Options to be copied, can be <code>null</code>.
     */
    public Options(Options options)
    {
        if (options != null)
        {
            for (String key : options.keys())
            {
                putUnencoded(key,options.get(key));
            }
        }
    }

    public Options(String key, Object value)
    {
        putUnencoded(key, value);
    }

    public Options put(String key, Object value)
    {
        String json = optionsJSON.forValue(value);
        content.put(key, json);
        return this;
    }

    public Options putUnencoded(String key, Object value)
    {
        content.put(key, value);
        return this;
    }

    public Options key(Object key)
    {
        return put("key",key);
    }

    public Options startKey(Object key)
    {
        return put("startkey",key);
    }

    public Options startKeyDocId(String docId)
    {
        return putUnencoded("startkey_docid", docId);
    }

    public Options endKey(Object key)
    {
        return put("endkey",key);
    }

    public Options endKeyDocId(String docId)
    {
        return putUnencoded("endkey_docid", docId);
    }
    
    public Options limit(int limit)
    {
        return putUnencoded("limit", limit);
    }

    public Options update(boolean update)
    {
        return putUnencoded("update",update);
    }

    public Options descending(boolean update)
    {
        return putUnencoded("descending",update);
    }

    public Options skip(int skip)
    {
        return putUnencoded("skip",skip);
    }

    public Options group(boolean group)
    {
        return putUnencoded("group",group);
    }

    public Options reduce(boolean reduce)
    {
        return putUnencoded("reduce",reduce);
    }

    public Options includeDocs(boolean includeDocs)
    {
        return putUnencoded("include_docs",includeDocs);
    }

    public String toQuery()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("?");

        boolean first = true;
        try
        {
            for (String key : keys())
            {
                if (!first)
                {
                    sb.append("&");
                }
                sb.append(key).append("=");
                sb.append(URLEncoder.encode(get(key).toString(), "UTF-8"));
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

    public Object get(String key)
    {
        return content.get(key);
    }

    /**
     * Can be imported statically to have a syntax a la <code>option().count(1);</code>.
     * @return new Option instance
     */
    public static Options option()
    {
        return new Options();
    }
    
    public Set<String> keys()
    {
        return content.keySet();
    }
    
}
