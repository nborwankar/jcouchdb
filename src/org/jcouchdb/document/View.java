package org.jcouchdb.document;


/**
 * Encapsulates a view inside a {@link DesignDocument}.
 *
 * @author shelmberger
 *
 */
public class View
{
    private String map,reduce;

    public View()
    {

    }

    public View(String mapFn)
    {
        setMap(mapFn);
    }

    public View(String mapFn, String reduceFn)
    {
        setMap(mapFn);
        setReduce(reduceFn);
    }

    public String getMap()
    {
        return map;
    }

    public void setMap(String map)
    {
        if (map != null && map.length() > 0)
        {
            this.map = map;
        }
        else
        {
            this.map = null;
        }
    }

    public String getReduce()
    {
        return reduce;
    }

    public void setReduce(String reduce)
    {
        if (reduce != null && reduce.length() > 0)
        {
            this.reduce = reduce;
        }
        else
        {
            this.reduce = null;
        }
    }

    @Override
    public String toString()
    {
        return super.toString()+": map = '"+map+", reduce='"+reduce+"'";
    }
}
