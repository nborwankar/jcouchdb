package org.jcouchdb.document;

import java.util.ArrayList;
import java.util.List;

import org.svenson.JSONTypeHint;

/**
 * Encapsulates the result of a view query.
 *
 * @author shelmberger
 *
 * @param <T>   type of the view result rows.
 */
public class ViewResult<T> extends BaseDocument
{
    private int totalRows;

    private int offset;

    private List<ViewResultRow<T>> rows = new ArrayList<ViewResultRow<T>>();

    public int getTotalRows()
    {
        return totalRows;
    }

    public void setTotalRows(int totalRows)
    {
        this.totalRows = totalRows;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public List<ViewResultRow<T>> getRows()
    {
        return rows;
    }

    @JSONTypeHint(ViewResultRow.class)
    public void setRows(List<ViewResultRow<T>> rows)
    {
        this.rows = rows;
    }

    @JSONTypeHint(ViewResultRow.class)
    public void setViews(List<ViewResultRow<T>> rows)
    {
        this.rows = rows;
    }

    @Override
    public String toString()
    {
        return super.toString()+": totalRows = "+totalRows+", offset = "+offset+", rows = "+rows;
    }
}
