package org.jcouchdb.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jcouchdb.db.LocalDatabaseTestCase;
import org.jcouchdb.document.DesignDocument;
import org.junit.Test;


public class JarBasedCouchDBUpdaterTestCase
{
    @Test
    public void test() throws IOException
    {
        JarBasedCouchDBUpdater couchDBUpdater = new JarBasedCouchDBUpdater();
        couchDBUpdater.setJarFile(new File("test/org/jcouchdb/util/views.jar"));
        couchDBUpdater.setPathInsideJar("test/path/test-views/");
        couchDBUpdater.setDatabase(LocalDatabaseTestCase.createDatabaseForTest());
        
        List<DesignDocument> docs = couchDBUpdater.readDesignDocuments();
        
        new CouchDBUpdaterTestCase().testDocsIntegrity( docs);
    }
}
