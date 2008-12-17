package org.jcouchdb.spring;

import java.io.IOException;

import org.jcouchdb.db.Database;
import org.jcouchdb.util.CouchDBUpdater;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

/**
 * Integrates the {@link CouchDBUpdater} into spring by making it an {@link InitializingBean}.
 * @author shelmberger
 *
 */
public class SpringCouchDBUpdater extends CouchDBUpdater implements InitializingBean
{
    public void setDesignDocumentDir(Resource designDocumentDir) throws IOException
    {
        super.setDesignDocumentDir(designDocumentDir.getFile());
    }

    @Override
    @Required
    public void setDatabase(Database database)
    {
        super.setDatabase(database);
    }

    public void afterPropertiesSet() throws Exception
    {
        updateDesignDocuments();
    }
}
