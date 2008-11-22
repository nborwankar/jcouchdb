package org.jcouchdb.exception;

import org.jcouchdb.db.Response;

/**
 * Is thrown when a document is not found.
 *
 * @author shelmberger
 *
 */
public class NotFoundException
    extends DataAccessException
{
    private static final long serialVersionUID = -4000164119397684440L;

    public NotFoundException(String message, Response resp)
    {
        super(message, resp);
    }
}
