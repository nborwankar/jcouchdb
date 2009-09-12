package org.jcouchdb.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.jcouchdb.document.DesignDocument;

public class JarBasedCouchDBUpdater
extends AbstractCouchDBUpdater
{

    private File jarFile;

    private String pathInsideJar;

    public void setPathInsideJar(String pathInsideJar)
    {
        this.pathInsideJar = pathInsideJar;
    }

    public void setJarFile(File jarFile)
    {
        Assert.isTrue(jarFile.exists(), "jarLocation must exist");
        Assert.isTrue(!jarFile.isDirectory(), "jarLocation must actually be a file");
        this.jarFile = jarFile;
    }

    @Override
    protected List<DesignDocument> readDesignDocuments() throws IOException
    {
        JarFile jarFile = new JarFile(this.jarFile);

        Map<String, DesignDocument> designDocuments = new HashMap<String, DesignDocument>();
        for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); )
        {
            JarEntry entry = e.nextElement();

            String name = entry.getName();
            boolean isMapFunction = name.endsWith(MAP_SUFFIX);
            boolean isReduceFunction = name.endsWith(REDUCE_SUFFIX);
            if (isMapFunction || isReduceFunction)
            {

                if (name.startsWith(pathInsideJar))
                {
                    log.debug("found map or reduce function: {}", name);
                    
                    String content = IOUtils.toString(jarFile.getInputStream(entry));
                    createViewFor(name.substring(pathInsideJar.length()), content, designDocuments);
                }
            }
        }

        return new ArrayList<DesignDocument>(designDocuments.values());
    }
}
