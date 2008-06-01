package org.jcouchdb.json;

import org.jcouchdb.json.parse.JSONTokenizer;

/**
 * Decides what type to convert a JSON to.
 * @author shelmberger
 */
public interface TokenInspector
{
    /**
     * Returns the type to use for the current tokenizer position
     *
     * @param tokenizer         tokenizer to get the tokens from. does not need to be resetted to the initial position.
     * @param parsePathInfo     the current parsing path within the root object
     * @param typeHint          initial type hint or <code>null</code>
     *
     * @return type hint or <code>null</code>
     */
    Class getTypeHint(JSONTokenizer tokenizer, String parsePathInfo, Class typeHint);
}
