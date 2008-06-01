package org.jcouchdb.db;

import org.junit.Test;


public class DesignDocumentTestCase
{
    @Test
    public void toJson()
    {
        DesignDocument designDocument = new DesignDocument("test");

        View view = new View();
        view.setMap("function(doc) { if (doc.foo) map(null,doc) }");
        designDocument.addView("foo", view );

    }

}
