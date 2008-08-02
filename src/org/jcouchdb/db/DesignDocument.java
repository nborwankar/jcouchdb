package org.jcouchdb.db;

import java.util.HashMap;
import java.util.Map;

import org.jcouchdb.json.JSONTypeHint;
import org.jcouchdb.util.Util;

public class DesignDocument
    extends BaseDocument
{
    private static final String PREFIX_UNESCAPED = "_design/";

    public final static String PREFIX = "_design%2F";

    private String language = "javascript";

    private Map<String, View> views = new HashMap<String, View>();

    public DesignDocument(String id, String revision)
    {
        setId(id);
        setRevision(revision);
    }

    public DesignDocument()
    {
        this(null, null);
    }

    public DesignDocument(String id)
    {
        this(id, null);
    }

    /**
     * Sets the id for the design document ( the "_design/" prefix which will be
     * added automatically )
     */
    @Override
    public void setId(String id)
    {
        super.setId(extendId(id));
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    @JSONTypeHint(View.class)
    public Map<String, View> getViews()
    {
        return views;
    }

    public void setViews(Map<String, View> views)
    {
        this.views = views;
    }

    public void addView(String name, View view)
    {
        views.put(name, view);
    }

    public static String extendId(String id)
    {
        if (id != null)
        {
            if (id.startsWith(PREFIX_UNESCAPED))
            {
                id = PREFIX + id.substring(PREFIX_UNESCAPED.length());
            }
            else if (!id.startsWith(PREFIX))
            {
                id = PREFIX + id;
            }
        }
        return id;
    }

    /**
     * Equality based on id, language and view comparison <em>without</em> revision comparison.
     * @param that
     * @return
     */
    public boolean equalsIncludingContent(DesignDocument that)
    {
        return Util.equals(this.getId(), that.getId()) &&
               Util.equals(this.getLanguage(), that.getLanguage()) &&
               Util.equals(this.getViews(), that.getViews());

    }

}
