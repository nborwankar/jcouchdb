package org.jcouchdb.util;


public class Util
{
    public static boolean equals(Object a, Object b)
    {
        return ((a == null && b == null) || (a != null && a.equals(b)));
    }

}
