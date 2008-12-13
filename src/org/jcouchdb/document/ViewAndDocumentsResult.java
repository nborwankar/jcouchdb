package org.jcouchdb.document;

import java.util.ArrayList;
import java.util.List;

import org.svenson.JSONTypeHint;

public class ViewAndDocumentsResult<V,D> extends AbstractViewResult
{
    List<DocumentRow<V,D>> rows = new ArrayList<DocumentRow<V,D>>();

    public List<DocumentRow<V,D>> getRows()
    {
        return rows;
    }

    @JSONTypeHint(DocumentRow.class)
    public void setRows(List<DocumentRow<V,D>> rows)
    {
        this.rows = rows;
    }

    @Override
    public String toString()
    {
        return super.toString()+ ", value rows = " + rows;
    }

}
