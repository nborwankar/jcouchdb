package org.jcouchdb.spring;

import org.jcouchdb.util.CouchDBUpdater;
import org.springframework.beans.factory.InitializingBean;

/**
 * Integrates the {@link CouchDBUpdater} into spring by making it an {@link InitializingBean}.
 * @author shelmberger
 *
 */
public class SpringCouchDBUpdater extends CouchDBUpdater implements InitializingBean
{
    public void afterPropertiesSet() throws Exception
    {
        updateDesignDocuments();
    }
}
