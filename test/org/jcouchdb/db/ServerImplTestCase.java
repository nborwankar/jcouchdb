package org.jcouchdb.db;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;

import org.easymock.IMocksControl;
import org.jcouchdb.document.BaseDocument;
import org.junit.Test;
import org.svenson.JSON;


public class ServerImplTestCase
{


    @Test
    public void testEventDelegation() throws Exception
    {
        BaseDocument doc = new BaseDocument();
        {
            IMocksControl ctrl = createControl();

            ServerEventHandler mock = ctrl.createMock(ServerEventHandler.class);

            Database db = LocalDatabaseTestCase.createDatabaseForTest();
            db.getServer().addServerEventHandler(mock);

            doc.setProperty("foo", "bar!");

            String docJSON = JSON.defaultJSON().forValue(doc);
            mock.executing(eq("post"), eq("/jcouchdb_test/"), eq(docJSON));
            mock.executed(eq("post"), eq("/jcouchdb_test/"), eq(docJSON), (Response)anyObject());

            ctrl.replay();

            db.createDocument(doc);

            ctrl.verify();
        }

        {
            IMocksControl ctrl = createControl();

            ServerEventHandler mock = ctrl.createMock(ServerEventHandler.class);

            Database db = LocalDatabaseTestCase.createDatabaseForTest();
            db.getServer().addServerEventHandler(mock);

            byte[] testData = "This is a Test text".getBytes();

            mock.executing(eq("put"), eq("/jcouchdb_test/"+doc.getId()+"test/test"), aryEq(testData));
            mock.executed(eq("put"), eq("/jcouchdb_test/"+doc.getId()+"test/test"), aryEq(testData), (Response)anyObject());

            ctrl.replay();

            db.createAttachment(doc.getId()+"test", null, "test", "text/plain", testData);

            ctrl.verify();
        }

        {
            IMocksControl ctrl = createControl();

            ServerEventHandler mock = ctrl.createMock(ServerEventHandler.class);

            Database db = LocalDatabaseTestCase.createDatabaseForTest();
            db.getServer().addServerEventHandler(mock);

            doc.setProperty("foo", "bar52");

            String docJSON = JSON.defaultJSON().forValue(doc);
            mock.executing(eq("put"), eq("/jcouchdb_test/"+doc.getId()), eq(docJSON));
            mock.executed(eq("put"), eq("/jcouchdb_test/"+doc.getId()), eq(docJSON), (Response)anyObject());

            ctrl.replay();

            db.updateDocument(doc);

            ctrl.verify();
        }

        {
            IMocksControl ctrl = createControl();

            ServerEventHandler mock = ctrl.createMock(ServerEventHandler.class);

            Database db = LocalDatabaseTestCase.createDatabaseForTest();
            db.getServer().addServerEventHandler(mock);

            mock.executing(eq("delete"), eq("/jcouchdb_test/"+doc.getId()+"?rev="+doc.getRevision()), eq(null));
            mock.executed(eq("delete"), eq("/jcouchdb_test/"+doc.getId()+"?rev="+doc.getRevision()), eq(null), (Response)anyObject());

            ctrl.replay();

            db.delete(doc);

            ctrl.verify();
        }
    }
}
