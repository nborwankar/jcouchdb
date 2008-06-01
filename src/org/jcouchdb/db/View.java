package org.jcouchdb.db;

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
