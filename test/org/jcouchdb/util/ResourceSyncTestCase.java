package org.jcouchdb.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.Map;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.LocalDatabaseTestCase;
import org.jcouchdb.document.Attachment;
import org.jcouchdb.document.BaseDocument;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ResourceSyncTestCase
{
    private static Logger log = LoggerFactory
        .getLogger(ResourceSyncTestCase.class);
    
    @Test
    public void test()
    {
        ResourceSync sync = new ResourceSync();
        Database db = LocalDatabaseTestCase.createDatabaseForTest();
        
        LocalDatabaseTestCase.deleteDocIfExists(db, sync.getResourceBaseDocId());
        
        sync.setDatabase(db);
        sync.setResourceBaseDir(new File("test/org/jcouchdb/util/resource-base/"));
        sync.syncResources();
        
        BaseDocument doc = db.getDocument(BaseDocument.class, sync.getResourceBaseDocId());
        Map<String, Attachment> map = doc.getAttachments();
        

        System.out.println(map);
        assertThat(map.size(), is(2));
        assertThat(map.get("test.txt").getLength(), is((3l)));
        assertThat(map.get("sub/test.txt").getLength(), is((3l)));
    }
}
