package org.jcouchdb.db;

public class DataAccessException extends CouchDBException
{
    private Response response;

    private static final long serialVersionUID = -3213554102218403815L;

    protected DataAccessException(String message, Response response)
    {
        super(message +": "+ message(response));
        this.response = response;
    }

    protected DataAccessException(Response response)
    {
        super(response.toString());
        this.response = response;
    }

    private static String message(Response response)
    {
        String message = "code "+response.getCode();

        final String content = response.getContent();
        if (content != null && content.length() > 0)
        {
            message+=", content = "+content;
        }

        return message;
    }

    public Response getResponse()
    {
        return this.response;
    }
}
