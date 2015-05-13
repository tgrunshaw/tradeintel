/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.kevindoran.highcharts.example;

import javax.faces.bean.ManagedBean;
import nz.co.kevindoran.highcharts.ColumnSeries;

/**
 *
 * @author kevin
 */
// Obviously, inheritence, or some other resue mechanism would typically be used
// other than just copying the class code.
@ManagedBean
public class NZEthnicPopulation2006 {
    private final ColumnSeries nzEuropeanSeries = new ColumnSeries();
    private final ColumnSeries maoriSeries = new ColumnSeries();
    private final ColumnSeries pacificSeries = new ColumnSeries();
    private final ColumnSeries asianSeries = new ColumnSeries();

    private static final String[] ageGroups = {"0-14", "15-39", "40-64", "65 and Over", "All ages"};
    private static final int[] nzEuropeanData = {73, 71, 81, 91, 77};
    private static final int[] maoriData = {24, 17, 10, 5, 15};
    private static final int[] asianData = {9, 13, 8, 4, 10};
    private static final int[] pacificData = {12, 8, 5, 2, 7};
    
    public NZEthnicPopulation2006() {
        populateSeries();
    }

    // Populate as in fill.
    private void populateSeries() {
        for(int i=0; i<ageGroups.length; i++) {
            nzEuropeanSeries.addColumn(ageGroups[i], nzEuropeanData[i]);
            maoriSeries.addColumn(ageGroups[i], maoriData[i]);
            pacificSeries.addColumn(ageGroups[i], pacificData[i]);
            asianSeries.addColumn(ageGroups[i], asianData[i]);
        }
    }

    public ColumnSeries getNzEuropeanSeries() {
        return nzEuropeanSeries;
    }

    public ColumnSeries getMaoriSeries() {
        return maoriSeries;
    }

    public ColumnSeries getPacificSeries() {
        return pacificSeries;
    }

    public ColumnSeries getAsianSeries() {
        return asianSeries;
    }
}