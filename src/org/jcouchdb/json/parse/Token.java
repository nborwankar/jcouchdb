package org.jcouchdb.json.parse;

import org.jcouchdb.util.Util;

/**
 * A JSON parsing token
 *
 * @author shelmberger
 *
 */
public class Token
{
    private Object value;
    private TokenType type;
    private int index;

    public Token(TokenType type, int index)
    {
        this(type,null,index);

    }

    /**
     * Creates a new Token with given value and the given token type
     *
     * @param type
     * @param value
     */
    public Token(TokenType type, Object value,int index)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type must be given");
        }

        type.checkValue(value);

        this.type = type;
        this.value = value;
        this.index = index;
    }

    public Object value()
    {
        return value;
    }

    public TokenType type()
    {
        return type;
    }

    public int getIndex()
    {
        return index;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Token)
        {
            Token that = (Token)obj;

            return this.type == that.type && Util.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 37+17*type.hashCode();

        if (value != null)
        {
            hash += 17 * value.hashCode();
        }

        return hash;
    }

    public boolean isType(TokenType type)
    {
        return this.type == type;
    }

    @Override
    public String toString()
    {
        return super.toString()+": "+type+" "+value;
    }
}
