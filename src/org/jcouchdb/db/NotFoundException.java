package org.jcouchdb.db;

public class NotFoundException
    extends DataAccessException
{
    private static final long serialVersionUID = -4000164119397684440L;

    public NotFoundException(String message, Response resp)
    {
        super(message, resp);
    }
}
