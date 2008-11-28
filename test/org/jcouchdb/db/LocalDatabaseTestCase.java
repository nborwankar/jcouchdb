package org.jcouchdb.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.document.Document;
import org.jcouchdb.document.DocumentInfo;
import org.jcouchdb.document.View;
import org.jcouchdb.document.ViewResult;
import org.jcouchdb.document.ViewResultRow;
import org.jcouchdb.exception.DataAccessException;
import org.jcouchdb.exception.NotFoundException;
import org.jcouchdb.exception.UpdateConflictException;
import org.junit.Test;
import org.svenson.JSON;

/**
 * Runs tests against a real couchdb database running on localhost
 *
 * @author shelmberger
 */
public class LocalDatabaseTestCase
{
    private JSON jsonGenerator = new JSON();

    private final static String COUCHDB_HOST = "localhost";

    private final static int COUCHDB_PORT = Server.DEFAULT_PORT;

    private static final String TESTDB_NAME = "jcouchdb_test";

    private static final String MY_FOO_DOC_ID = "myFooDocId";

    private static final String BY_VALUE_FUNCTION = "function(doc) { if (doc.type == 'foo') { emit(doc.value,doc); }  }";

    private static final String COMPLEX_KEY_FUNCTION = "function(doc) { if (doc.type == 'foo') { emit([1,{\"value\":doc.value}],doc); }  }";

    protected static Logger log = Logger.getLogger(LocalDatabaseTestCase.class);

    private Database createDatabaseForTest()
    {
        return new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);
    }

    @Test
    public void recreateTestDatabase()
    {
        try
        {
            Server server = new ServerImpl(COUCHDB_HOST, COUCHDB_PORT);
            List<String> databases = server.listDatabases();

            log.debug("databases = " + databases);

            if (databases.contains(TESTDB_NAME))
            {
                server.deleteDatabase(TESTDB_NAME);
            }

            server.createDatabase(TESTDB_NAME);

        }
        catch (RuntimeException e)
        {
            log.error("", e);
        }
    }

    @Test
    public void createTestDocuments()
    {
        Database db = createDatabaseForTest();

        FooDocument foo = new FooDocument("bar!");

        assertThat(foo.getId(), is(nullValue()));
        assertThat(foo.getRevision(), is(nullValue()));

        db.createDocument(foo);

        assertThat(foo.getId(), is(notNullValue()));
        assertThat(foo.getRevision(), is(notNullValue()));

        foo = new FooDocument("baz!");
        foo.setProperty("baz2", "Some test value");

        db.createDocument(foo);

        log.debug("-- resetted database ----------------------------------");
    }

    @Test
    public void thatMapDocumentsWork()
    {
        Database db = createDatabaseForTest();

        Map<String,String> doc = new HashMap<String, String>();

        doc.put("foo", "value for the foo attribute");
        doc.put("bar", "value for the bar attribute");

        db.createDocument(doc);

        final String id = doc.get("_id");
        assertThat(id, is(notNullValue()));
        assertThat(doc.get("_rev"), is(notNullValue()));

        doc = db.getDocument(Map.class, id);

        assertThat(doc.get("foo"), is("value for the foo attribute"));
        assertThat(doc.get("bar"), is("value for the bar attribute"));

    }

    @Test
    public void thatCreateNamedDocWorks()
    {
        FooDocument doc = new FooDocument("qux");
        doc.setId(MY_FOO_DOC_ID);

        Database db = createDatabaseForTest();
        db.createDocument(doc);
        assertThat(doc.getId(), is(MY_FOO_DOC_ID));
        assertThat(doc.getRevision(), is(notNullValue()));

    }

    @Test
    public void thatUpdateDocWorks()
    {
        Database db = createDatabaseForTest();

        FooDocument doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
        assertThat(doc.getValue(), is("qux"));

        doc.setValue("qux!");
        db.updateDocument(doc);

        doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
        assertThat(doc.getValue(), is("qux!"));
    }

    @Test(expected = UpdateConflictException.class)
    public void thatUpdateConflictWorks()
    {
        FooDocument doc = new FooDocument("qux");
        doc.setId(MY_FOO_DOC_ID);

        new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME).createDocument(doc);
    }

    @Test
    public void testGetAll()
    {
        Database db = createDatabaseForTest();

        ViewResult<Map> result = db.listDocuments();

        List<ViewResultRow<Map>> rows = result.getRows();

        assertThat(rows.size(), is(4));

        String json = jsonGenerator.forValue(rows);
        log.debug("rows = " + json);
    }

    @Test
    public void testCreateDesignDocument()
    {
        Database db = createDatabaseForTest();

        DesignDocument designDocument = new DesignDocument("foo");
        designDocument.addView("byValue", new View(BY_VALUE_FUNCTION));
        designDocument.addView("complex", new View(COMPLEX_KEY_FUNCTION));
        log.debug("DESIGN DOC = " + jsonGenerator.dumpObjectFormatted(designDocument));

        db.createDocument(designDocument);
    }

    @Test
    public void getDesignDocument()
    {
        Database db = createDatabaseForTest();
        DesignDocument doc = db.getDesignDocument("foo");
        log.debug(jsonGenerator.dumpObjectFormatted(doc));
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getId(), is(DesignDocument.PREFIX + "foo"));
        assertThat(doc.getViews().get("byValue").getMap(), is(BY_VALUE_FUNCTION));
    }

    @Test
    public void queryDocuments()
    {
        Database db = createDatabaseForTest();
        ViewResult<FooDocument> result = db.queryView("foo/byValue", FooDocument.class);

        assertThat(result.getRows().size(), is(3));

        FooDocument doc = result.getRows().get(0).getValue();
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getValue(), is("bar!"));

        doc = result.getRows().get(1).getValue();
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getValue(), is("baz!"));

        doc = result.getRows().get(2).getValue();
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getId(), is(MY_FOO_DOC_ID));
        assertThat(doc.getValue(), is("qux!"));

    }

    @Test
    public void queryDocumentsWithComplexKey()
    {
        Database db = createDatabaseForTest();
        ViewResult<FooDocument> result = db.queryView("foo/complex", FooDocument.class);

        assertThat(result.getRows().size(), is(3));

        ViewResultRow<FooDocument> row = result.getRows().get(0);
        assertThat(jsonGenerator.forValue(row.getKey()), is("[1,{\"value\":\"bar!\"}]"));

    }

    @Test
    //@Ignore
    public void thatGetDocumentWorks()
    {
        Database db = createDatabaseForTest();
        FooDocument doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
        assertThat(doc.getId(), is(MY_FOO_DOC_ID));
        assertThat(doc.getRevision(), is(notNullValue()));
        assertThat(doc.getValue(), is("qux!"));

        log.debug(jsonGenerator.dumpObjectFormatted(doc));

    }

    @Test
    //@Ignore
    public void thatAdHocViewsWork()
    {
        Database db = createDatabaseForTest();
        ViewResult<FooDocument>  result = db.queryAdHocView(FooDocument.class, "{ \"map\" : \"function(doc) { if (doc.baz2 == 'Some test value') emit(null,doc);  } \" }");

        assertThat(result.getRows().size(), is(1));

        FooDocument doc = result.getRows().get(0).getValue();
        assertThat((String)doc.getProperty("baz2"), is("Some test value"));
    }

    @Test
    public void thatNonDocumentFetchingWorks()
    {
        Database db = createDatabaseForTest();
        NotADocument doc = db.getDocument(NotADocument.class, MY_FOO_DOC_ID);
        assertThat(doc.getId(), is(MY_FOO_DOC_ID));
        assertThat(doc.getRevision(), is(notNullValue()));
        assertThat((String)doc.getProperty("value"), is("qux!"));

        log.debug(jsonGenerator.dumpObjectFormatted(doc));

        doc.setProperty("value", "changed");

        db.updateDocument(doc);

        NotADocument doc2 = db.getDocument(NotADocument.class, MY_FOO_DOC_ID);
        assertThat((String)doc2.getProperty("value"), is("changed"));

    }


    @Test
    public void thatBulkCreationWorks()
    {
        Database db = createDatabaseForTest();

        List<Document> docs = new ArrayList<Document>();

        docs.add(new FooDocument("doc-1"));
        docs.add(new FooDocument("doc-2"));
        docs.add(new FooDocument("doc-3"));
        List<DocumentInfo> infos = db.bulkCreateDocuments(docs);

        assertThat(infos.size(), is(3));

    }

    @Test
    public void thatBulkCreationWithIdsWorks()
    {
        Database db = createDatabaseForTest();

        List<Document> docs = new ArrayList<Document>();

        FooDocument fooDocument = new FooDocument("doc-2");
        fooDocument.setId("second-foo-with-id");
        docs.add(new FooDocument("doc-1"));

        docs.add(fooDocument);
        docs.add(new FooDocument("doc-3"));

        List<DocumentInfo> infos = db.bulkCreateDocuments(docs);

        assertThat(infos.size(), is(3));

        assertThat(infos.get(0).getId().length(), is(greaterThan(0)));
        assertThat(infos.get(1).getId(), is("second-foo-with-id"));
        assertThat(infos.get(2).getId().length(), is(greaterThan(0)));

    }

    @Test(expected=UpdateConflictException.class)
    public void thatUpdateConflictsWork()
    {
        FooDocument foo = new FooDocument("value foo");
        FooDocument foo2 = new FooDocument("value foo2");
        foo.setId("update_conflict");
        foo2.setId("update_conflict");

        Database db = createDatabaseForTest();
        db.createDocument(foo);
        db.createDocument(foo2);

    }

    @Test
    public void thatDeleteWorks()
    {
        FooDocument foo = new FooDocument("a document");
        Database db = createDatabaseForTest();
        db.createDocument(foo);

        assertThat(foo.getId(), is ( notNullValue()));

        FooDocument foo2 = db.getDocument(FooDocument.class, foo.getId());

        assertThat(foo.getValue(), is(foo2.getValue()));

        db.delete(foo);

        try
        {
            db.getDocument(FooDocument.class, foo.getId());
            throw new IllegalStateException("document shouldn't be there anymore");
        }
        catch(NotFoundException nfe)
        {
            // yay!
        }
    }

    @Test(expected = DataAccessException.class)
    public void thatDeleteFailsIfWrong()
    {
        Database db = createDatabaseForTest();
        db.delete("fakeid", "fakrev");
    }
}
