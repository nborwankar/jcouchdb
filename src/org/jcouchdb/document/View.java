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
        this.map = map;
    }

    public String getReduce()
    {
        return reduce;
    }

    public void setReduce(String reduce)
    {
        this.reduce = reduce;
    }
}
