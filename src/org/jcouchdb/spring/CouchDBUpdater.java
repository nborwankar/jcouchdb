package org.jcouchdb.spring;

import java.util.List;

import org.apache.log4j.Logger;
import org.jcouchdb.db.Database;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.exception.NotFoundException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class CouchDBUpdater implements InitializingBean
{
    protected static Logger log = Logger.getLogger(CouchDBUpdater.class);

    private Database database;

    private boolean createDatabase = true;

    @Required
    public void setDatabase(Database database)
    {
        this.database = database;
    }

    public void setCreateDatabase(boolean createDatabase)
    {
        this.createDatabase = createDatabase;
    }

    private List<DesignDocument> designDocuments;

    @Required
    public void setDesignDocuments(List<DesignDocument> designDocuments)
    {
        this.designDocuments = designDocuments;
    }

    public void afterPropertiesSet() throws Exception
    {
        updateDesignDocuments();
    }

    private Database createOrGetDatabase()
    {
        if (createDatabase)
        {
            String databaseName = database.getName();
            if (database.getServer().createDatabase(databaseName))
            {
                if (log.isInfoEnabled())
                {
                    log.info("Database \""+databaseName+"\" created.");
                }
            }
            else
            {
                if (log.isInfoEnabled())
                {
                    log.info("Database \""+databaseName+"\" already exists.");
                }
            }
        }

        return database;
    }

    public void updateDesignDocuments()
    {
        Database database = createOrGetDatabase();

        for (DesignDocument designDocument : designDocuments)
        {
            DesignDocument existing = null;
            try
            {
                existing  = database.getDesignDocument(designDocument.getId());
            }
            catch (NotFoundException e)
            {
                // ignore
            }

            boolean shouldUpdate = existing == null || !existing.equalsIncludingContent(designDocument);
            if ( shouldUpdate )
            {
                if (existing != null)
                {
                    designDocument.setRevision(existing.getRevision());
                    if (log.isDebugEnabled())
                    {
                        log.debug("updating design document "+designDocument+" with revision "+designDocument.getRevision());
                    }
                }
                database.createOrUpdateDocument(designDocument);
            }
        }
    }
}
