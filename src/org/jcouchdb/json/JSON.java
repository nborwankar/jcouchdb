package org.jcouchdb.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Tool class with only static methods used to construct <a
 * href="http://json.org">JSON</a> representations of nested java object
 * graphs. The object graphs can contain the following components:
 * <ul>
 * <li>Primitives, Booleans and Strings</li>
 * <li>Javabeans</li>
 * <li>Arrays</li>
 * <li>Collections</li>
 * <li>Maps</li>
 * </ul>
 *
 * @author Sven Helmberger ( sven dot helmberger at gmx dot de )
 * @version $Id: $
 * @license BSD revised
 * @see JSONable
 * @see JSONifier
 */
public class JSON
{
    /** Prevent instantiation. */
    private JSON()
    {
    }

    private static Map<Class,JSONifier> jsonifiers=Collections.synchronizedMap(new HashMap<Class, JSONifier>());

    public static void registerJSONifier(Class c, JSONifier jsonifier)
    {
        jsonifiers.put(c, jsonifier);
    }

    public static void deregisterJSONifiers()
    {
        jsonifiers.clear();
    }

    /**
     * Dumps the given object as formatted JSON representation. The method dumps
     * the object to JSON first and reformats it then, so it's not the fastest
     * method. It is mainly thought to provide debug output or similar.
     *
     * @param o
     *          Object
     * @return JSON representation
     */
    public static String dumpObjectFormatted(Object o)
    {
        StringBuilder out = new StringBuilder();
        dumpObject(out, o);

        String s = out.toString();
        StringBuilder sb = new StringBuilder(s.length() * 3 / 2);
        StringTokenizer st = new StringTokenizer(s, "{}[],\"", true);
        int icnt = 0;
        String lastToken="";
        boolean quoted=false;
        while (st.hasMoreTokens())
        {
            String token = st.nextToken();

            if (token.equals("\""))
            {
                int pos=lastToken.length()-1;
                int cnt=0;
                while (pos >= 0 && lastToken.charAt(pos) == '\\')
                {
                    pos--;
                    cnt++;
                }

                if ((cnt & 1) == 0)
                {
                    quoted=!quoted;
                }
            }

            if (quoted)
            {
                sb.append(token);
            }
            else
            {
                if (token.equals("{") || token.equals("["))
                {
                    icnt++;
                    sb.append(token);
                    newLine(sb, icnt);
                }
                else if (token.equals("}") || token.equals("]"))
                {
                    icnt--;
                    newLine(sb, icnt);
                    sb.append(token);
                }
                else if (token.equals(","))
                {
                    sb.append(token);
                    newLine(sb, icnt);
                }
                else
                {
                    sb.append(token);
                }
            }
            lastToken=token;
        }
        return sb.toString();
    }

    private final static String INDENT = "  ";

    private final static String NEWLINE = System.getProperty("line.separator");

    /**
     * Adds a newline followed by a given number of indentation spaces.
     *
     * @param sb
     *          StringStringBuilder
     * @param cnt
     *          level of indentation
     */
    private static void newLine(StringBuilder sb, int cnt)
    {
        sb.append(NEWLINE);
        for (int i = 0; i < cnt; i++)
        {
            sb.append(INDENT);
        }
    }

    /**
     * Dumps the given object as JSON representation into the given
     * StringBuilder
     *
     * @param out
     *          StringBuilder
     * @param o
     *          objct
     */
    public static void dumpObject(StringBuilder out, Object o)
    {
        dumpObject(out, o, '\0', null);
    }

    /**
     * Dumps the given object as JSON representation into the given
     * StringBuilder
     *
     * @param out
     *          StringBuilder
     * @param o
     *          objct
     * @param ignoredProps
     *          List containing the properties to be ignored.
     */
    public static void dumpObject(StringBuilder out, Object o,
            List<String> ignoredProps)
    {
        dumpObject(out, o, '\0', ignoredProps);
    }

    /**
     * Dumps the given object as JSON representation followed by a separator
     * into the given StringBuilder.
     *
     * @param out
     *          StringBuilder
     * @param o
     *          object
     * @param separator
     *          separator character to append after the object or
     *          <code>'\0'</code> to append no separator.
     */
    private static void dumpObject(StringBuilder out, Object o, char separator,
            List<String> ignoredProps)
    {
        try
        {
            if (o == null)
            {
                out.append("null");
            }
            else
            {
                Class oClass = o.getClass();

                JSONifier jsonifier;

                if (oClass.isPrimitive())
                {
                    out.append(o);
                }
                else if (Number.class.isAssignableFrom(oClass)
                        || oClass.equals(Boolean.class)
                        || oClass.equals(Character.class))
                {
                    out.append(o);
                }
                else if (o instanceof String)
                {
                    quote(out, (String) o);
                }
                else if (o instanceof Collection)
                {
                    out.append("[");
                    for (Iterator i = ((Collection) o).iterator(); i.hasNext();)
                    {
                        dumpObject(out, i.next(), i.hasNext() ? ',' : '\0',
                                ignoredProps);
                    }
                    out.append("]");
                }
                else if (o.getClass().isArray())
                {
                    out.append("[");
                    int len = Array.getLength(o);
                    for (int i = 0; i < len; i++)
                    {
                        dumpObject(out, Array.get(o, i), ((i < (len - 1)) ? ','
                                : '\0'), ignoredProps);
                    }
                    out.append("]");
                }
                else if (o instanceof Map)
                {
                    out.append("{");
                    Map m = (Map) o;
                    for (Iterator i = m.keySet().iterator(); i.hasNext();)
                    {
                        Object key = i.next();
                        dumpObject(out, key, '\0', ignoredProps);
                        out.append(":");
                        dumpObject(out, m.get(key), i.hasNext() ? ',' : '\0',
                                ignoredProps);
                    }
                    out.append("}");
                }
                else if((jsonifier=jsonifiers.get(oClass)) != null)
                {
                    out.append(jsonifier.toJSON(o));
                }
                else if (o instanceof JSONable)
                {
                    out.append(((JSONable) o).toJSON());
                }
                else
                {
                    BeanInfo info = Introspector.getBeanInfo(o.getClass());
                    out.append("{");
                    PropertyDescriptor[] pds = info.getPropertyDescriptors();
                    for (int cp = 0; cp < pds.length; cp++)
                    {
                        PropertyDescriptor pd = pds[cp];
                        Method method = pd.getReadMethod();
                        Method writeMethod = pd.getWriteMethod();
                        if (method != null)
                        {
                            Object value = method.invoke(o, (Object[]) null);
                            String name = pd.getName();
                            boolean ignore = (ignoredProps != null && ignoredProps.contains(name));
                            if (!name.equals("class") && !ignore)
                            {
                                JSONProperty jsonProperty = method.getAnnotation(JSONProperty.class);
                                if (jsonProperty == null && writeMethod != null)
                                {
                                    jsonProperty = writeMethod.getAnnotation(JSONProperty.class);
                                }
                                if (jsonProperty != null)
                                {
                                    name = jsonProperty.value();

                                    ignore = jsonProperty.ignore() || ( value == null && jsonProperty.ignoreIfNull() );
                                }

                                if (!ignore)
                                {
                                    quote(out, name);
                                    out.append(':');
                                    dumpObject(out, value,
                                            cp < (pds.length - 1) ? ',' : '\0',
                                            ignoredProps);
                                }
                            }
                        }
                    }

                    if (o instanceof DynamicAttrs)
                    {
                        DynamicAttrs dynAttrs = (DynamicAttrs)o;
                        for (String name : dynAttrs.attributeNames())
                        {
                            out.append(',');
                            quote(out, name);
                            out.append(':');
                            dumpObject(out, dynAttrs.getAttribute(name),'\0',ignoredProps);
                        }
                    }

                    out.append("}");
                }
            }
            if (separator != '\0')
            {
                out.append(separator);
            }
        }
        catch (IntrospectionException ie)
        {
            throw new RuntimeException("Can't introspect bean ", ie);
        }
        catch (IllegalAccessException iae)
        {
            throw new RuntimeException("Can't access bean property ", iae);
        }
        catch (InvocationTargetException ite)
        {
            throw new RuntimeException("Error getting bean property ", ite);
        }
    }

    /**
     * Returns a JSON representation of the given object as String.
     *
     * @return JSON representation
     * @param o
     *          object
     */
    public static String forValue(Object o)
    {
        StringBuilder tmp = new StringBuilder();
        dumpObject(tmp, o);
        return tmp.toString();
    }

    /**
     * Returns a JSON representation of the given object as String.
     *
     * @return JSON representation
     * @param o
     *          object
     */
    public static String forValue(Object o, List<String> ignoredProps)
    {
        StringBuilder tmp = new StringBuilder();
        dumpObject(tmp, o, ignoredProps);
        return tmp.toString();
    }

    /**
     * Inserts the given String as quoted and escaped, JSON-conform String into
     * the given StringBuilder.
     *
     * @param buf
     *          StringBuilder
     * @param s
     *          String to quote and escape
     */
    public static void quote(StringBuilder buf, String s)
    {
        try
        {
            if (s == null)
            {
                buf.append("null");
                return;
            }

            buf.append("\"");
            for (int i = 0; i < s.length(); i++)
            {
                char c = s.charAt(i);
                switch (c)
                {
                    case '"':
                        buf.append("\\\"");
                        break;
                    case '/':
                        buf.append("\\/");
                        break;
                    case '\\':
                        buf.append("\\\\");
                        break;
                    case '\b':
                        buf.append("\\b");
                        break;
                    case '\f':
                        buf.append("\\f");
                        break;
                    case '\n':
                        buf.append("\\n");
                        break;
                    case '\r':
                        buf.append("\\r");
                        break;
                    case '\t':
                        buf.append("\\t");
                        break;
                    default:
                        if (c < 32 || c > 127)
                        {
                            String h = Integer.toHexString(c);
                            int len = h.length();
                            if (len < 4)
                            {
                                h = "0000".substring(len) + h;
                            }
                            buf.append("\\u" + h);
                        }
                        else
                        {
                            buf.append(c);
                        }
                        break;
                }
            }
            buf.append("\"");

        }
        catch (Exception e)
        {
            // shouldn't happen
            throw new RuntimeException("error quoting JSON string", e);
        }
    }

    public static String quote(String s)
    {
        StringBuilder sb=new StringBuilder();
        quote(sb,s);
        return sb.toString();
    }
}
