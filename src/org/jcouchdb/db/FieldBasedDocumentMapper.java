package org.jcouchdb.db;

import java.util.HashMap;
import java.util.Map;

import org.jcouchdb.json.TypeMapper;
import org.jcouchdb.json.parse.JSONTokenizer;
import org.jcouchdb.json.parse.Token;
import org.jcouchdb.json.parse.TokenType;

/**
 * Uses a discriminator field to convert the documents of a query into
 * configured types.
 *
 * For example if you have documents like
 *
 * <code>{ "type" : "foo" , ... }</code> and
 *
 * <code>{ "type" : "bar" , ... }</code>,
 *
 * you can use the following code to convert them into the appropriate
 * types:
 *
<pre><code>
        JSONParser parser = new JSONParser();
        FieldBasedDocumentMapper mapper = new FieldBasedDocumentMapper();
        mapper.addFieldValueMapping("foo", Foo.class);
        mapper.addFieldValueMapping("bar", Bar.class);
        parser.setTypeMapper(mapper);

        ViewResult<Map> result = db.listDocuments(null, parser);
</code></pre>

 * See the TestCase for this class for a full example.
 *
 * @author shelmberger
 */
public class FieldBasedDocumentMapper
    implements TypeMapper
{
    private Map<String, Class> fieldValueToType = new HashMap<String, Class>();

    private String discriminatorField = "type";

    private boolean allowUndefined;

    private String parsePathInfo = Database.DOCUMENT_TYPE_PATH;

    /**
     *
     *
     * @param value
     * @param cls
     */
    public void addFieldValueMapping(String value, Class cls)
    {
        fieldValueToType.put(value, cls);
    }

    /**
     * Sets the parse path info at which the type discrimination is applied.
     * Default is <code>".rows[].value"</code> which is the parse path info for
     * a couchdb view result transformation. This method is only present for
     * complete configurability of the {@link FieldBasedDocumentMapper}, use
     * only if you know what you're doing.
     *
     * @param parsePathInfo
     */
    public void setParsePathInfo(String parsePathInfo)
    {
        this.parsePathInfo = parsePathInfo;
    }

    /**
     * If set to <code>true</code>, java.util.HashMap will be used for discriminator field values that are mapped to no class.
     * Otherwise an exception is thrown in that case.
     * @param allowUndefined
     */
    public void setAllowUndefined(boolean allowUndefined)
    {
        this.allowUndefined = allowUndefined;
    }

    /**
     * Sets the property used to discriminate between the different document
     * types
     *
     * @param discriminatorField
     */
    public void setDiscriminatorField(String discriminatorField)
    {
        this.discriminatorField = discriminatorField;
    }

    public Class getTypeHint(JSONTokenizer tokenizer, String parsePathInfo, Class typeHint)
    {
        if (this.parsePathInfo.equals(parsePathInfo))
        {
            Token first = tokenizer.next();
            tokenizer.pushBack(first);

            try
            {
                Token token;
                while ((token = tokenizer.next()).type() != TokenType.END)
                {
                    token.expect(TokenType.STRING);
                    String propertyName = (String) token.value();
                    tokenizer.expectNext(TokenType.COLON);

                    Token firstValueToken = tokenizer.next();

                    if (propertyName.equals(discriminatorField))
                    {
                        firstValueToken.expect(TokenType.STRING);
                        String fieldValue = (String) firstValueToken.value();
                        Class hint = getTypeHintFromTypeProperty(fieldValue);
                        if (hint == null && !allowUndefined)
                        {
                            throw new IllegalStateException("There is no class mapped for the value \""+fieldValue+"\" of discriminator field "+discriminatorField+" and undefined values are not allowed");
                        }
                        return hint;
                    }
                    else
                    {
                        if (firstValueToken.type() == TokenType.BRACE_OPEN)
                        {
                            skipObjectValue(tokenizer);
                        }
                        else if (firstValueToken.type() == TokenType.BRACKET_OPEN)
                        {
                            skipArrayValue(tokenizer);
                        }

                        Token next = tokenizer.expectNext(TokenType.COMMA, TokenType.BRACE_CLOSE);
                        if (next.type() == TokenType.BRACE_CLOSE)
                        {
                            return HashMap.class;
                        }
                    }
                }
                return null;
            }
            finally
            {
                tokenizer.pushBack(first);
            }
        }
        return typeHint;
    }

    private Class getTypeHintFromTypeProperty(String value)
    {
        Class cls = fieldValueToType.get(value);
        return cls == null ? HashMap.class : cls;
    }

    public void skipObjectValue(JSONTokenizer tokenizer)
    {
        int objLevel = 1;

        Token token;
        TokenType tokenType;
        while ((tokenType = (token = tokenizer.next()).type()) != TokenType.END)
        {
            if (tokenType == TokenType.BRACE_OPEN)
            {
                objLevel++;
            }
            else if (tokenType == TokenType.BRACE_CLOSE)
            {
                objLevel--;
            }

            if (objLevel == 0)
            {
                break;
            }
        }

        if (token.type() == TokenType.END)
        {
            throw new IllegalStateException("Unexpected end");
        }
    }

    public void skipArrayValue(JSONTokenizer tokenizer)
    {
        int arrayLevel = 1;

        Token token;
        TokenType tokenType;
        while ((tokenType = (token = tokenizer.next()).type()) != TokenType.END)
        {
            if (tokenType == TokenType.BRACKET_OPEN)
            {
                arrayLevel++;
            }
            else if (tokenType == TokenType.BRACKET_CLOSE)
            {
                arrayLevel--;
            }

            if (arrayLevel == 0)
            {
                break;
            }
        }

        if (token.type() == TokenType.END)
        {
            throw new IllegalStateException("Unexpected end");
        }
    }
}
