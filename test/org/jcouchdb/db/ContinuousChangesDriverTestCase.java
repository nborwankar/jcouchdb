package org.jcouchdb.db;

import java.util.ArrayList;
import java.util.List;

import org.jcouchdb.document.BaseDocument;
import org.jcouchdb.document.ChangeListener;
import org.jcouchdb.document.ChangeNotification;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContinuousChangesDriverTestCase
{
    private static Database db;

    private static Logger log = LoggerFactory.getLogger(ContinuousChangesDriverTestCase.class);
    
    
    @BeforeClass
    public static void createDB()
    {
        db = LocalDatabaseTestCase.recreateDB("continuous-changes");
    }


//    @AfterClass
//    public static void deleteDB()
//    {
//        db.getServer().deleteDatabase(db.getName());
//    }


    @Test
    public void test()
    {
        TestListener listener = new TestListener();
        db.registerChangeListener(null, null, null, listener);
        
        db.createDocument( newDoc("foo","123"));
        db.createDocument( newDoc("bar","456"));
        
        
        db.getServer().shutDown();
    }
    
    static class TestListener implements ChangeListener
    {
        private List<ChangeNotification> changeNotifications = new ArrayList<ChangeNotification>();
        public void onChange(ChangeNotification changeNotification)
        {
            log.info("notification: {}", changeNotification);
            
            changeNotifications.add(changeNotification);
        }
        
        public List<ChangeNotification> getChangeNotifications()
        {
            return changeNotifications;
        }
    }

    public BaseDocument newDoc(String name, String data)
    {
        BaseDocument doc = new BaseDocument();
        doc.setProperty("_id", name);
        doc.setProperty("data", data);
        return doc;
    }
}
