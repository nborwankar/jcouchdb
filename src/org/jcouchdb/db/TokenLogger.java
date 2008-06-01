package org.jcouchdb.db;

import org.apache.log4j.Logger;
import org.jcouchdb.json.TokenInspector;
import org.jcouchdb.json.parse.JSONTokenizer;

public class TokenLogger
    implements TokenInspector
{
    protected static Logger log = Logger.getLogger(TokenLogger.class);
    public Class getTypeHint(JSONTokenizer tokenizer, String parsePathInfo, Class typeHint)
    {
        if (log.isDebugEnabled())
        {
            log.debug("parsePathInfo = "+parsePathInfo+", typeHint = "+typeHint);
        }
        return typeHint;
    }

}
