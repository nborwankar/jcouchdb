package org.jcouchdb.db;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jcouchdb.document.BaseDocument;
import org.jcouchdb.document.ViewResult;
import org.jcouchdb.document.ViewResultRow;
import org.junit.Test;
import org.svenson.JSONParser;
import org.svenson.PropertyValueBasedTypeMapper;


public class DocumentTypeFieldBasedDiscriminationTestCase
{
    protected static Logger log = Logger.getLogger(DocumentTypeFieldBasedDiscriminationTestCase.class);

    @Test
    public void thatItWorks() throws IOException
    {
        Server serverMock = createMock(Server.class);

        String json = FileUtils.readFileToString(new File("test/org/jcouchdb/db/multi-type-result.json"));

        expect(serverMock.get("/test/_all_docs")).andReturn(new Response(200,json)).anyTimes();

        replay(serverMock);

        Database db = new Database(serverMock, "test");

        JSONParser parser = new JSONParser();
        PropertyValueBasedTypeMapper mapper = new PropertyValueBasedTypeMapper();
        mapper.setParsePathInfo(Database.DOCUMENT_TYPE_PATH);
        mapper.addFieldValueMapping("foo", Foo.class);
        mapper.addFieldValueMapping("bar", Bar.class);
        parser.setTypeMapper(mapper);

        ViewResult<Map> result = db.listDocuments(null, parser);

        List<ViewResultRow<Map>> rows = result.getRows();
        assertThat(rows.size(), is(3));

        Foo foo = (Foo)rows.get(0).getValue();

        assertThat(foo.getValue(), is("aaa"));

        Bar bar= (Bar)rows.get(1).getValue();
        assertThat(bar.getValue(), is("bbb"));
        bar = (Bar)rows.get(2).getValue();
        assertThat(bar.getValue(), is("ccc"));

        verify(serverMock);
    }

    public static class Foo extends BaseDocument
    {
        private String type, value;

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return super.toString()+": value = "+value;
        }
    }

    public static class Bar extends Foo
    {

    }
}
