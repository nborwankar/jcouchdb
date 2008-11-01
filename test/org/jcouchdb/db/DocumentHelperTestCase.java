package org.jcouchdb.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.jcouchdb.db.BaseDocument;
import org.jcouchdb.db.DocumentHelper;
import org.junit.Test;


public class DocumentHelperTestCase
{

    @Test
    public void thatGetIdWorks()
    {
        Bean b = new Bean();
        b.set_id("foo");

        assertThat(DocumentHelper.getId(b), is(("foo")));

        BaseDocument doc = new BaseDocument();
        doc.setId("foo");
        assertThat(DocumentHelper.getId(doc), is(("foo")));
    }


    @Test
    public void thatSetIdWorks()
    {
        Bean b = new Bean();
        b.set_id("foo");
        DocumentHelper.setId(b, "bar");
        assertThat(b.get_id(), is(("bar")));

        BaseDocument doc = new BaseDocument();
        doc.setId("foo");
        DocumentHelper.setId(doc, "bar");
        assertThat(doc.getId(), is(("bar")));
    }

    @Test
    public void thatGetRevWorks()
    {
        Bean b = new Bean();
        b.set_rev("foo");
        assertThat(DocumentHelper.getRevision(b), is(("foo")));

        BaseDocument doc = new BaseDocument();
        doc.setRevision("foo");
        assertThat(DocumentHelper.getRevision(doc), is(("foo")));
    }


    @Test
    public void thatSetRevWorks()
    {
        Bean b = new Bean();
        b.set_rev("foo");
        DocumentHelper.setRevision(b, "bar");
        assertThat(b.get_rev(), is(("bar")));

        BaseDocument doc = new BaseDocument();
        doc.setRevision("foo");
        DocumentHelper.setRevision(doc, "bar");
        assertThat(doc.getRevision(), is(("bar")));
    }

    public static class Bean
    {
        private String _id, _rev;

        public String get_id()
        {
            return _id;
        }

        public void set_id(String _id)
        {
            this._id = _id;
        }

        public String get_rev()
        {
            return _rev;
        }

        public void set_rev(String _rev)
        {
            this._rev = _rev;
        }

    }
}
