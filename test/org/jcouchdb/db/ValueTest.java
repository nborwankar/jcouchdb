package org.jcouchdb.db;

import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.ViewResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueTest
{
    private static Logger log = LoggerFactory.getLogger(ValueTest.class);
    
    public static void main(String[] args)
    {
        Database db = new Database("localhost", "test");

        ViewResult<ValueType> result = db.queryView("test/idToNameAndValue", ValueType.class, null,null);
        
        for (ValueRow<ValueType> row : result.getRows())
        {
            log.info("Value = {}", row.getValue());
        }
        
    }
    
    public static class ValueType
    {
        private String name;
        private int value;

        public String getName()
        {
            return name;
        }


        public void setName(String name)
        {
            this.name = name;
        }


        public int getValue()
        {
            return value;
        }


        public void setValue(int value)
        {
            this.value = value;
        }


        @Override
        public String toString()
        {
            return super.toString() + "[name=" + name + ", value=" + value + "]";
        }
    }
}
