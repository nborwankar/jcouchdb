package org.jcouchdb.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


public class MediaTypeUtilTestCase
{
    @Test
    public void testMediaType()
    {
        MediaTypeUtil util = new MediaTypeUtil();
        
        assertThat(util.getMediaTypeForName("test.gif"), is("image/gif"));
        assertThat(util.getMediaTypeForName("test.png"), is("image/png"));
        assertThat(util.getMediaTypeForName("test.jpg"), is("image/jpeg"));
        assertThat(util.getMediaTypeForName("test.html"), is("text/html"));
        assertThat(util.getMediaTypeForName("test.css"), is("text/css"));
        assertThat(util.getMediaTypeForName("test.js"), is("application/x-javascript"));
    }
}