package org.jcouchdb.json;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.jcouchdb.json.parse.JSONParseException;
import org.jcouchdb.json.parse.JSONTokenizer;
import org.jcouchdb.json.parse.Token;
import org.jcouchdb.json.parse.TokenType;
import org.jcouchdb.util.DocumentHelper;
import org.jcouchdb.util.ExceptionWrapper;

/**
 * Converts JSON strings into graphs of java objects. It offers
 * features to support the full spectrum of totally dynamic parsing
 * to parsing into concrete java types, including a mix in between.
 *
 * @author shelmberger
 *
 * @see JSONProperty
 * @see DynamicAttrs
 * @see #addTypeHint(String, Class)
 * @see #setTypeHints(Map)
 */
public class JSONParser
{
    protected static Logger log = Logger.getLogger(JSONParser.class);

    private Map<String, Class> typeHints = new HashMap<String, Class>();

    private TokenInspector tokenInspector;

    public void setTokenInspector(TokenInspector tokenInspector)
    {
        this.tokenInspector = tokenInspector;
    }

    public void setTypeHints(Map<String, Class> typeHints)
    {
        this.typeHints = typeHints;
    }

    /**
     * Sets a type hint for a given parsing path location.
     * Locations matching that type hint use the mapped
     * class as bean to parse the JSON into.
     *  <p>
     *  for example: using a type hint
     *  <br><br>
     *  <code>parser.setTypeHint(".foo[]", Foo.class);</code>
     *  <br><br>
     *   would map a json string like:
     *
     *  <pre>
     *  {
     *      "foo" : [ ... ]
     *  }
     *  </pre>
     *
     *  so that the values inside the array of the foo property
     *  of the root object are mapped to Foo instances.
     *
     */
    public void addTypeHint(String key, Class typeHint)
    {
        this.typeHints.put(key, typeHint);
    }

    public Object parse( String json)
    {
        JSONTokenizer tokenizer = new JSONTokenizer(json);

        Token token = tokenizer.next();
        if (token.isType(TokenType.BRACKET_OPEN))
        {
            return parse( ArrayList.class, json);
        }
        else if (token.isType(TokenType.BRACE_OPEN))
        {
            return parse( HashMap.class, json);
        }
        else
        {
            throw new JSONParseException("Expected [ or { but found "+token);
        }
    }


    /**
     * Parses a JSON String
     * @param <T> The type to parse the root object into
     * @param targetType   Runtime class for <T>
     * @param json  json string
     * @return the resulting object
     */
    public <T> T parse(Class<T> targetType, String json)
    {
        JSONTokenizer tokenizer = new JSONTokenizer(json);

        if (targetType == null)
        {
            throw new IllegalArgumentException("target type cannot be null");
        }

        if (json == null)
        {
            throw new IllegalArgumentException("json string cannot be null");
        }

        T t;
        try
        {
            Token token = tokenizer.next();
            TokenType type = token.type();
            if (type == TokenType.BRACE_OPEN)
            {
                t = (T) createNewTargetInstance(targetType, "", tokenizer, "", true);
                parseObjectInto(new ParseContext(t,null), tokenizer);
            }
            else if (type == TokenType.BRACKET_OPEN)
            {
                t = (T) createNewTargetInstance(targetType, "", tokenizer, "", false);
                parseArrayInto(new ParseContext(t,null), tokenizer);
            }
            else
            {
                throw new JSONParseException("unexpected token "+token);
            }
            return t;
        }
        catch (InstantiationException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IllegalAccessException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (InvocationTargetException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (NoSuchMethodException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }

    /**
     * Expects the next token to be of one of the given token types
     *
     * @param tokenizer
     * @param type
     * @return
     * @throws JSONParseException if the expectation is not fulfilled
     */
    private Token expectNext(JSONTokenizer tokenizer, TokenType... type)
    {
        Token t = tokenizer.next();
        expect(t,type);
        return t;
    }

    /**
     * Expects the given token to be of one of the given token types
     *
     * @param tokenizer
     * @param type
     * @return
     * @throws JSONParseException if the expectation is not fulfilled
     */
    private void expect(Token t, TokenType... types)
    {
        for (TokenType type : types)
        {
            if (t.type() == type)
            {
                return;
            }
        }
        throw new JSONParseException("Unexpected token "+t);
    }

    /**
     * Expects the next object of the given tokenizer to be an array and parses it into the given {@link ParseContext}
     * @param cx
     * @param tokenizer
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private void parseArrayInto(ParseContext cx, JSONTokenizer tokenizer) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        boolean containerIsCollection = Collection.class.isAssignableFrom(cx.target.getClass());

        boolean first = true;
        while(true)
        {
            Token valueToken = tokenizer.next();
            TokenType valueType = valueToken.type();
            if (valueType == TokenType.BRACKET_CLOSE)
            {
                break;
            }

            if (!first)
            {
                expect(valueToken, TokenType.COMMA);
                valueToken = tokenizer.next();
                valueType = valueToken.type();
            }

            Object value;
            if (valueType.isPrimitive())
            {
                value = valueToken.value();

                if(cx.memberType != null)
                {
                    value = convertValueTo(value, cx.memberType);
                }
            }
            else
            {
                Object newTarget = null;
                if (valueType == TokenType.BRACE_OPEN)
                {
                    newTarget = createNewTargetInstance(cx.memberType, cx.getParsePathInfo("[]"), tokenizer, "[]", true);
                    parseObjectInto(cx.push(newTarget,null,"[]"), tokenizer);
                }
                else if (valueType == TokenType.BRACKET_OPEN)
                {
                    newTarget = createNewTargetInstance(cx.memberType, cx.getParsePathInfo("[]"), tokenizer, "[]", false);
                    parseArrayInto(cx.push(newTarget,null,"[]"), tokenizer);
                }
                else
                {
                    throw new JSONParseException("Unexpected token "+valueToken);
                }
                value = newTarget;
            }

            if (containerIsCollection)
            {
                ((Collection)cx.target).add(value);
            }
            else
            {
                throw new JSONParseException("Cannot add value "+value+" to "+cx.target);
            }

            first = false;
        }
    }

    private void parseObjectInto(ParseContext cx, JSONTokenizer tokenizer) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        boolean containerIsMap = Map.class.isAssignableFrom(cx.target.getClass());
        boolean containerIsDynAttrs = DynamicAttrs.class.isAssignableFrom(cx.target.getClass());

        boolean first = true;
        while (true)
        {
            Token key ;
            if (first)
            {
                key = expectNext(tokenizer, TokenType.STRING, TokenType.BRACE_CLOSE);
            }
            else
            {
                key = expectNext(tokenizer, TokenType.COMMA, TokenType.BRACE_CLOSE);

            }
            if (key.type() == TokenType.BRACE_CLOSE)
            {
                break;
            }

            if (!first)
            {
                key = expectNext(tokenizer, TokenType.STRING);
            }

            String name = DocumentHelper.getPropertyNameFromAnnotation(cx.target, (String)key.value());
            if (name.length() == 0)
            {
                throw new JSONParseException("Invalid empty property name");
            }

            expectNext(tokenizer, TokenType.COLON);

            Token valueToken = tokenizer.next();

            TokenType valueType = valueToken.type();

            boolean isProperty = PropertyUtils.isWriteable(cx.target, name);

            Method addMethod = getAddMethod(cx.target, name);

            if (!(isProperty || containerIsMap ||containerIsDynAttrs || addMethod != null))
            {
                throw new JSONParseException("Cannot set property "+name+" on "+cx.target.getClass());
            }

            Object value;
            if (valueType.isPrimitive())
            {
                value = valueToken.value();
            }
            else
            {
                Object newTarget = null;
                if (valueType == TokenType.BRACE_OPEN)
                {
                    Class memberType = null;

                    if (isProperty)
                    {
                        memberType = getTypeHintFromAnnotation(cx, name);
                    }

                    newTarget = createNewTargetInstance(cx.memberType, cx.getParsePathInfo(name), tokenizer, name, true);
                    parseObjectInto(cx.push(newTarget, memberType, "."+name), tokenizer);
                }
                else if (valueType == TokenType.BRACKET_OPEN)
                {
                    //Class memberType = null;

                    if (isProperty)
                    {
                        newTarget = createNewTargetInstance(cx.memberType, cx.getParsePathInfo(name), tokenizer, name, false);
                        Class memberType = getTypeHintFromAnnotation(cx, name);
                        parseArrayInto(cx.push(newTarget,memberType, "."+name), tokenizer);
                    }
                    else
                    {

                        if (addMethod != null)
                        {
                            Class memberType = addMethod.getParameterTypes()[0];
                            List temp = new ArrayList();
                            parseArrayInto(cx.push(temp,memberType, "."+name), tokenizer);

                            for (Object o : temp)
                            {
                                addMethod.invoke(cx.target, o);
                            }
                            continue;
                        }
                        else
                        {
                            throw new JSONParseException("Cannot set array to property "+name+" on "+cx.target);
                        }
                    }
                }
                else
                {
                    throw new JSONParseException("Unexpected token "+valueToken);
                }

                value = newTarget;
            }

            if (isProperty)
            {
                try
                {
                    Class targetClass = PropertyUtils.getPropertyType(cx.target, name);
                    PropertyUtils.setProperty(cx.target, name, convertValueTo(value, targetClass));
                }
                catch (IllegalAccessException e)
                {
                    throw ExceptionWrapper.wrap(e);
                }
                catch (InvocationTargetException e)
                {
                    throw ExceptionWrapper.wrap(e);
                }
                catch (NoSuchMethodException e)
                {
                    throw ExceptionWrapper.wrap(e);
                }
            }
            else if (containerIsMap)
            {
                ((Map)cx.target).put( name, value);
            }
            else if (containerIsDynAttrs)
            {
                ((DynamicAttrs)cx.target).setAttribute(name, value);
            }
            else
            {
                throw new JSONParseException("Cannot set property "+name+" on "+cx.target);
            }

            first = false;
        } // end while
    }

    private Class replaceKnownInterfaces(Class type)
    {
        if (type != null && type.isInterface())
        {
            if (Map.class.isAssignableFrom(type))
            {
                return HashMap.class;
            }
            else if (List.class.isAssignableFrom(type))
            {
                return ArrayList.class;
            }
            else
            {
                throw new JSONParseException("Cannot instantiate interface "+type);
            }
        }
        return type;
    }



    private Method getAddMethod(Object bean, String name)
    {
        String addMethodName = "add"+name.substring(0,1).toUpperCase()+name.substring(1);
        for (Method m : bean.getClass().getMethods())
        {
            if (m.getName().equals(addMethodName) && (m.getModifiers() & Modifier.PUBLIC) != 0 && m.getParameterTypes().length == 1 )
            {
                return m;
            }
        }
        return null;
    }

    private <T> T convertValueTo(Object value, Class<T> targetClass)
    {
        if (targetClass == null)
        {
            throw new IllegalArgumentException("target class is null");
        }
        if (value == null)
        {
            return null;
        }

        if (!targetClass.isAssignableFrom(value.getClass()))
        {
            value = ConvertUtils.convert(value.toString(), targetClass);
        }
        return (T)value;
    }


    private Object createNewTargetInstance(Class typeHint, String parsePathInfo, JSONTokenizer tokenizer, String name, boolean object)
    {
        Class cls = getTypeHint( parsePathInfo,tokenizer,name);

        if (cls != null)
        {
            typeHint = cls;
        }

        if (typeHint == null || typeHint.equals(Object.class))
        {
            if (object)
            {
                typeHint = Map.class;
            }
            else
            {
                typeHint = List.class;
            }
        }

        if (typeHint.isInterface())
        {
            typeHint = replaceKnownInterfaces(typeHint);
        }

        try
        {
            return typeHint.newInstance();
        }
        catch (InstantiationException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (IllegalAccessException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }

    private Class getTypeHint(String parsePathInfo, JSONTokenizer tokenizer, String name)
    {

        Class typeHint = typeHints.get(parsePathInfo);

        if (log.isDebugEnabled())
        {
            log.debug("info = "+parsePathInfo+ " => "+typeHint);
        }

        if (tokenInspector != null)
        {
            typeHint = tokenInspector.getTypeHint(tokenizer, parsePathInfo, typeHint);
        }

        return typeHint;
    }

    private Class getTypeHintFromAnnotation(ParseContext cx, String name)
    {

        try
        {
            Object target = cx.target;
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(target, name);

            if (pd != null)
            {
                Method readMethod = pd.getReadMethod();
                Method writeMethod = pd.getWriteMethod();

                JSONTypeHint typeHintAnnotation = null;
                if (writeMethod != null)
                {
                    typeHintAnnotation = writeMethod.getAnnotation(JSONTypeHint.class);
                }
                if (typeHintAnnotation == null && readMethod != null)
                {
                    typeHintAnnotation = readMethod.getAnnotation(JSONTypeHint.class);
                }
                if (typeHintAnnotation != null)
                {
                    return typeHintAnnotation.value();
                }
            }

            return null;
        }
        catch (IllegalAccessException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (InvocationTargetException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
        catch (NoSuchMethodException e)
        {
            throw ExceptionWrapper.wrap(e);
        }
    }

    private class ParseContext
    {
        private Object target;
        private ParseContext parent;
        private Class memberType;
        private String info="";

        public ParseContext(Object target, Class memberType)
        {
            this(target,memberType,null);
        }

        private ParseContext(Object target, Class memberType, ParseContext parent)
        {
            this.target = target;
            this.parent = parent;
            this.memberType = memberType;
        }

        public ParseContext push(Object target, Class memberType, String info)
        {
            ParseContext child = new ParseContext(target, memberType, this);
            child.info = this.info + info;
            return child;

        }

        public ParseContext pop()
        {
            return parent;
        }

        public String getParsePathInfo(String name)
        {
            String parsePathInfo;
            if (name.equals("[]"))
            {
                parsePathInfo = info+name;
            }
            else
            {
                parsePathInfo = info+"."+name;
            }
            return parsePathInfo;
        }

        @Override
        public String toString()
        {
            return super.toString()+", target = "+target+", memberType = "+memberType+", info = "+info;
        }
    }
}
