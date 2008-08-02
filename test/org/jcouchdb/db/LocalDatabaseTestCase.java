package org.jcouchdb.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jcouchdb.json.JSON;
import org.junit.Test;

/**
 * Runs tests against a real couchdb database running on localhost
 *
 * @author shelmberger
 */
public class LocalDatabaseTestCase
{
    private final static String COUCHDB_HOST = "localhost";

    private final static int COUCHDB_PORT = Server.DEFAULT_PORT;

    private static final String TESTDB_NAME = "jcouchdb_test";

    private static final String MY_FOO_DOC_ID = "myFooDocId";

    private static final String BY_VALUE_FUNCTION = "function(doc) { if (doc.type == 'foo') { emit(doc.value,doc); }  }";

    private static final String COMPLEX_KEY_FUNCTION = "function(doc) { if (doc.type == 'foo') { emit([1,{\"value\":doc.value}],doc); }  }";

    protected static Logger log = Logger.getLogger(LocalDatabaseTestCase.class);

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
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);

        FooDocument foo = new FooDocument("bar!");

        assertThat(foo.getId(), is(nullValue()));
        assertThat(foo.getRevision(), is(nullValue()));

        db.createDocument(foo);

        assertThat(foo.getId(), is(notNullValue()));
        assertThat(foo.getRevision(), is(notNullValue()));

        foo = new FooDocument("baz!");
        foo.setAttribute("baz2", "Some test value");

        db.createDocument(foo);

        log.debug("-- resetted database ----------------------------------");
    }

    @Test
    public void thatMapDocumentsWork()
    {
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);

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

        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);
        db.createDocument(doc);
        assertThat(doc.getId(), is(MY_FOO_DOC_ID));
        assertThat(doc.getRevision(), is(notNullValue()));

    }

    @Test
    public void thatUpdateDocWorks()
    {
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);

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
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);

        ViewResult<Map> result = db.listDocuments();

        List<ViewResultRow<Map>> rows = result.getRows();

        assertThat(rows.size(), is(4));

        String json = JSON.forValue(rows);
        log.debug("rows = " + json);
    }

    @Test
    public void testCreateDesignDocument()
    {
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);

        DesignDocument designDocument = new DesignDocument("foo");
        designDocument.addView("byValue", new View(BY_VALUE_FUNCTION));
        designDocument.addView("complex", new View(COMPLEX_KEY_FUNCTION));
        log.debug("DESIGN DOC = " + JSON.dumpObjectFormatted(designDocument));

        db.createDocument(designDocument);
    }

    @Test
    public void getDesignDocument()
    {
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);
        DesignDocument doc = db.getDesignDocument("foo");
        log.debug(JSON.dumpObjectFormatted(doc));
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getId(), is(DesignDocument.PREFIX + "foo"));
        assertThat(doc.getViews().get("byValue").getMap(), is(BY_VALUE_FUNCTION));
    }

    @Test
    public void queryDocuments()
    {
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);
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
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);
        ViewResult<FooDocument> result = db.queryView("foo/complex", FooDocument.class);

        assertThat(result.getRows().size(), is(3));

        ViewResultRow<FooDocument> row = result.getRows().get(0);
        assertThat(JSON.forValue(row.getKey()), is("[1,{\"value\":\"bar!\"}]"));

    }

    @Test
    //@Ignore
    public void thatGetDocumentWorks()
    {
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);
        FooDocument doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
        assertThat(doc.getId(), is(MY_FOO_DOC_ID));
        assertThat(doc.getRevision(), is(notNullValue()));
        assertThat(doc.getValue(), is("qux!"));

        log.debug(JSON.dumpObjectFormatted(doc));

    }

    @Test
    //@Ignore
    public void thatAdHocViewsWork()
    {
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);
        ViewResult<FooDocument>  result = db.queryAdHocView(FooDocument.class, "{ \"map\" : \"function(doc) { if (doc.baz2 == \\\"Some test value\\\") emit(null,doc);  } \" }");

        assertThat(result.getRows().size(), is(1));

        FooDocument doc = result.getRows().get(0).getValue();
        assertThat((String)doc.getAttribute("baz2"), is("Some test value"));
    }

    @Test
    public void thatBulkCreationWorks()
    {
        Database db = new Database(COUCHDB_HOST, COUCHDB_PORT, TESTDB_NAME);

        Collection<Document> docs = new ArrayList<Document>();

        docs.add(new FooDocument("doc-1"));
        docs.add(new FooDocument("doc-2"));
        docs.add(new FooDocument("doc-3"));
        List<DocumentInfo> infos = db.bulkCreateDocuments(docs);

        assertThat(infos.size(), is(3));

    }
}
