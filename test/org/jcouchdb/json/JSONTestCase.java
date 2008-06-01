package org.jcouchdb.json;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import java.util.TreeMap;

import org.jcouchdb.json.JSON;
import org.jcouchdb.json.JSONable;
import org.jcouchdb.json.JSONifier;
import org.junit.Test;

public class JSONTestCase
{
    @Test
    public void testInt()
    {
        assertThat(JSON.forValue(52), is("52"));
    }

    @Test
    public void testString()
    {
        assertThat(JSON.forValue("Hello äöü"), is("\"Hello \\u00e4\\u00f6\\u00fc\""));
    }

    @Test
    public void testArray()
    {
        assertEquals("[1,2,3]", JSON.forValue(new int[] { 1,2,3}));
    }

    @Test
    public void testMap()
    {
        // treemap for ordered keys
        Map<String, Object> m=new TreeMap<String, Object>();
        m.put("foo", 42);
        m.put("bar", "baz");
        assertEquals("{\"bar\":\"baz\",\"foo\":42}", JSON.forValue(m));
    }

    @Test
    public void testBean()
    {
        String json=JSON.forValue(new SimpleBean(new int[]{1,2},"baz"));
        assertEquals("{\"bar\":\"baz\",\"foo\":[1,2]}",json);
    }

    @Test
    public void testJSONable()
    {
        JSONable jsonableMock = createMock(JSONable.class);

        final String json = "{\"foo\":42,\"bar\":[1,3,5]}";
        expect(jsonableMock.toJSON()).andReturn(json);
        replay(jsonableMock);
        assertThat(JSON.forValue(jsonableMock), is(json));
        verify(jsonableMock);
    }

    @Test
    public void thatJSONifierWorks()
    {
        JSON.deregisterJSONifiers();

        assertThat(JSON.forValue(new JSONifiedBean()), is("{}"));

        JSONifier jsonifierMock = createMock(JSONifier.class);
        final String jsonifierOutput = "{foo:1}";
        expect(jsonifierMock.toJSON(isA(JSONifiedBean.class))).andReturn(jsonifierOutput);

        JSON.registerJSONifier(JSONifiedBean.class, jsonifierMock);

        replay(jsonifierMock);
        String json = JSON.forValue(new JSONifiedBean());

        assertThat(json, is(jsonifierOutput));
        verify(jsonifierMock);
    }

    @Test
    public void thatJSONPropertyAnnotationsAreConsidered()
    {
        JSONPropertyAnnotatedBean bean = new JSONPropertyAnnotatedBean();
        bean.setFoo("foo!");
        bean.setBar("bar!");

        assertThat(JSON.forValue(bean), is("{\"baz\":\"foo!\"}"));

    }

    @Test
    public void thatDynAttrsAreDumped()
    {
        DynAttrsBean bean = new DynAttrsBean();
        bean.setFoo("fu!");
        bean.setAttribute("bar", "bar!");

        assertThat(JSON.forValue(bean), containsString("\"foo\":\"fu!\""));
        assertThat(JSON.forValue(bean), containsString("\"bar\":\"bar!\""));

    }

    public static class SimpleBean
    {
        private Object foo,bar;

        public SimpleBean(Object foo, Object bar)
        {
            this.foo=foo;
            this.bar=bar;
        }

        public Object getFoo()
        {
            return foo;
        }

        public Object getBar()
        {
            return bar;
        }
    }

    public static class JSONifiedBean
    {
    }

    public static class JSONPropertyAnnotatedBean
    {
        private String foo,bar;

        @JSONProperty("baz")
        public String getFoo()
        {
            return foo;
        }

        public void setFoo(String foo)
        {
            this.foo = foo;
        }

        public String getBar()
        {
            return bar;
        }

        @JSONProperty(ignore=true)
        public void setBar(String bar)
        {
            this.bar = bar;
        }
    }
}
