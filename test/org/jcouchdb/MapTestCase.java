package org.jcouchdb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MapTestCase
{

    @Test
    public void equality()
    {
        Map m1 = new HashMap<String, String>();
        Map m2 = new HashMap<String, String>();

        m1.put("foo", "foo!");
        m1.put("bar", "bar!");
        m2.put("foo", "foo!");
        m2.put("bar", "bar!");

        assertThat(m1.equals(m2), is(true));

        m2.put("bar", "esutrpeiu");

        assertThat(m1.equals(m2), is(false));
    }

}
