package org.jcouchdb.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jcouchdb.db.LocalDatabaseTestCase;
import org.jcouchdb.document.DesignDocument;
import org.junit.Test;


public class CouchDBUpdaterTestCase
{
    private static Logger log = Logger.getLogger(CouchDBUpdaterTestCase.class);

    @Test

    public void thatDesignDocumentReadingWorks() throws IOException
    {
        CouchDBUpdater couchDBUpdater = new CouchDBUpdater();
        couchDBUpdater.setDesignDocumentDir(new File("test/org/jcouchdb/util/test-views/"));
        couchDBUpdater.setDatabase(LocalDatabaseTestCase.createDatabaseForTest());


        List<DesignDocument> docs = couchDBUpdater.updateDesignDocuments();

        log.info(docs);

        assertThat(docs,is(notNullValue()));
        assertThat(docs.size(), is(2));
        assertThat(docs.get(0).getId(),is("_design/view1"));
        assertThat(docs.get(1).getId(),is("_design/view2/sub"));


    }

}
