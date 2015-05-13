package nz.co.kevindoran.highcharts;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kevin
 */
public class CartesianSeries {
    private final ArrayList<CartesianData> data = new ArrayList<>();

    public CartesianSeries() {
    }
    
    public void addData(Number x, Number y) {
        CartesianData cd = new CartesianData(x, y);
        data.add(cd);
    }
    
    public void addData(Number x, Number y, Map<String, Object> properties) {
        CartesianData cd = new CartesianData(x, y, properties);
        data.add(cd);
    }
    
    public List<CartesianData> getData() {
        return data;
    }
}
