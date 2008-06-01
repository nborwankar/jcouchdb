package org.jcouchdb;
import org.apache.log4j.Logger;
import org.junit.Test;


public class TestCase
{
    protected static Logger log = Logger.getLogger(TestCase.class);

    @Test
    public void testname() throws Exception
    {
        Object o1 = String.class;
        Object o2 = 1;

        log.debug(o1.getClass().equals(Class.class));
        log.debug(o2.getClass().equals(Class.class));
    }
}
