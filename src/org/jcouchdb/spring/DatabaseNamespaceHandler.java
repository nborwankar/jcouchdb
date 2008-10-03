package org.jcouchdb.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class DatabaseNamespaceHandler extends NamespaceHandlerSupport
{
    public void init()
    {
        registerBeanDefinitionParser("database", new DatabaseBeanDefinitionParser());
    }
}
