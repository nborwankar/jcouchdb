package org.jcouchdb.document;

import org.jcouchdb.util.Base64Util;
import org.svenson.JSONProperty;

/**
 * Wraps an attachment.
 *
 * @author shelmberger
 *
 */
public class Attachment
{
    private String contentType, data;
    private long length;
    private boolean stub;

    public Attachment()
    {

    }

    public Attachment(String contentType, byte[] data)
    {
        this.contentType = contentType;
        this.data = Base64Util.encodeBase64(data);
    }


    @JSONProperty("content_type")
    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public long getLength()
    {
        return length;
    }

    public void setLength(long length)
    {
        this.length = length;
    }

    public boolean isStub()
    {
        return stub;
    }

    public void setStub(boolean stub)
    {
        this.stub = stub;
    }
}
