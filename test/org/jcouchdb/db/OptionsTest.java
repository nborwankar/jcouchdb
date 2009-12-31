package org.jcouchdb.db;

import org.jcouchdb.document.ViewResult;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionsTest
{   
    private static Logger log = LoggerFactory.getLogger(OptionsTest.class);
    
    @Test
    public void test()
    {
        Database db = new Database("localhost", "test");
        
        ViewResult<Object> result = db.queryView("test/byNameAndValue", Object.class, 
            new Options().putUnencoded("startkey", "[\"foo\",null,null,null,null]").putUnencoded("endkey", "[\"foo\",{},{},{},{}]"),
            null);
        
        log.info("{}",result);
        
    }

}
