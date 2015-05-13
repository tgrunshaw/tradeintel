package nz.co.kevindoran.highcharts;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author kevin
 */
public class ColumnData {
    private String columnName;
    private Number columnValue;

    public ColumnData(String columnName, Number columnValue) {
        this.columnName = columnName;
        this.columnValue = columnValue;
    }
    
    public String getName() {
        return columnName;
    }

    public void setName(String columnName) {
        this.columnName = columnName;
    }

    public Number getValue() {
        return columnValue;
    }

    public void setValue(Number columnValue) {
        this.columnValue = columnValue;
    }
}
