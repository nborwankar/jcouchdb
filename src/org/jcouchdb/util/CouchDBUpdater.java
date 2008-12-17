package org.jcouchdb.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jcouchdb.db.Database;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.document.View;
import org.jcouchdb.exception.NotFoundException;

public class CouchDBUpdater
{
    private static final String REDUCE_SUFFIX = ".reduce.js";

    private static final String MAP_SUFFIX = ".map.js";

    protected static Logger log = Logger.getLogger(CouchDBUpdater.class);

    private Database database;

    private boolean createDatabase = true;

    private File designDocumentDir;

    public void setDesignDocumentDir(File designDocumentDir)
    {
        Assert.isTrue(designDocumentDir.exists(), "designDocumentDir must exist");
        Assert.isTrue(designDocumentDir.isDirectory(), "designDocumentDir must actually be a directory");
        this.designDocumentDir = designDocumentDir;
    }

    public void setDatabase(Database database)
    {
        this.database = database;
    }

    public void setCreateDatabase(boolean createDatabase)
    {
        this.createDatabase = createDatabase;
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

    public List<DesignDocument> updateDesignDocuments() throws IOException
    {
        Database database = createOrGetDatabase();

        List<DesignDocument> designDocuments = readDesignDocuments();
        for (DesignDocument designDocument : designDocuments )
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
        return designDocuments;
    }

    List<DesignDocument> readDesignDocuments() throws IOException
    {
        Assert.notNull(designDocumentDir, "designDocumentDir can't be null");

        Collection<File> files = FileUtils.listFiles(designDocumentDir, new String[]{ "js"}, true);

        Map<String,DesignDocument> designDocuments = new HashMap<String, DesignDocument>();

        String designDocumentDirPath = designDocumentDir.getPath();

        for (File file : files)
        {
            String path = file.getPath();
            Assert.isTrue(path.startsWith(designDocumentDirPath), "not in dir");

            path = path.substring(designDocumentDirPath.length());

            boolean isMapFunction = path.endsWith(MAP_SUFFIX);
            boolean isReduceFunction = path.endsWith(REDUCE_SUFFIX);
            if (isMapFunction || isReduceFunction)
            {
                String content = FileUtils.readFileToString(file);

                if (content == null || content.trim().length() == 0 )
                {
                    continue;
                }

                List<String> parts = StringUtil.split(path, File.separator);

                Assert.isTrue(parts.size() > 1, "invalid dir structure");

                String fnName = parts.remove(parts.size()-1);
                String viewId = StringUtil.join(parts, "/");

                DesignDocument designDocument = designDocuments.get(viewId);
                if (designDocument == null)
                {
                    designDocument = new DesignDocument(viewId);
                    designDocuments.put(viewId, designDocument);
                }

                if (isMapFunction)
                {
                    fnName = fnName.substring(0, fnName.length()-MAP_SUFFIX.length());
                }
                else
                {
                    fnName = fnName.substring(0, fnName.length()-REDUCE_SUFFIX.length());
                }

                View view = designDocument.getViews().get(fnName);
                if (view == null)
                {
                    if (isMapFunction)
                    {
                        view = new View( content, null);
                    }
                    else
                    {
                        view = new View( null, content);
                    }
                    designDocument.getViews().put(fnName, view);
                }
                else
                {
                    if (isMapFunction)
                    {
                        view.setMap(content);
                    }
                    else
                    {
                        view.setReduce(content);
                    }
                }

            }
        }

        return new ArrayList<DesignDocument>(designDocuments.values());
    }
}
