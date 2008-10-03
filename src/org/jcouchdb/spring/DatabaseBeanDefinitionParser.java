package org.jcouchdb.spring;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DatabaseBeanDefinitionParser
    implements BeanDefinitionParser
{
    protected static Logger log = Logger.getLogger(DatabaseBeanDefinitionParser.class);

    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder
            .rootBeanDefinition(CouchDBUpdater.class);

        List<Element> viewElements = DomUtils.getChildElementsByTagName(element, "view");
        if (viewElements != null && viewElements.size() > 0)
        {
            for (Element viewElement : viewElements)
            {
                parseView(viewElement, factory);
            }
        }
        return factory.getBeanDefinition();
    }

    private void parseView(Element viewElement, BeanDefinitionBuilder factory)
    {
        Element mapFunctionElement = DomUtils.getChildElementByTagName(viewElement, "map-function");
        Element reduceFunctionElement = DomUtils.getChildElementByTagName(viewElement, "reduce-function");

        //factory.addPropertyValue("map", value)

    }

}
