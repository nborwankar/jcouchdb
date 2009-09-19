package org.jcouchdb.util;

import static org.easymock.EasyMock.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
    
    @Test
    public void testFindClassPathJar()
    {
        JarBasedCouchDBUpdater couchDBUpdater = new JarBasedCouchDBUpdater();
        couchDBUpdater.setJarFilePattern(".*/easymock\\.jar");
        couchDBUpdater.setPathInsideJar("org/easymock");
        
        File f = couchDBUpdater.findJarFileOrSourceDirectory();
        
        assertThat(f.isDirectory(), is(false));

        String cdir = getCurrentDir();
        String path = f.getPath();
        assertThat(path.endsWith("easymock.jar"), is(true));
        assertThat(path.startsWith(cdir), is(true));
    }

    private String getCurrentDir()
    {
        String cdir = new File(".").getAbsolutePath();
        cdir = cdir.substring(0, cdir.length()-1);
        return cdir;
    }

    @Test
    public void testFindClassPathDir()
    {
        JarBasedCouchDBUpdater couchDBUpdater = new JarBasedCouchDBUpdater();
        couchDBUpdater.setJarFilePattern(".*/jcouchdb[0-9\\.]*jar");
        couchDBUpdater.setPathInsideJar("org/jcouchdb");
        
        File f = couchDBUpdater.findJarFileOrSourceDirectory();

        assertThat(f.isDirectory(), is(true));
        
        String cdir = getCurrentDir();
        String path = f.getPath();
        assertThat(path.startsWith(cdir), is(true));
    }
    
}
