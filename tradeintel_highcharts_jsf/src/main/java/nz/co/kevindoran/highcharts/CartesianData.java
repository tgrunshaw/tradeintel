package nz.co.kevindoran.highcharts;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Kevin
 */
public class CartesianData {
    private Number x;
    private Number y;
    private Map<String, Object> properties;
    
    public CartesianData(Number x, Number y) {
        this(x,y, Collections.<String, Object>emptyMap());
    }
    
    public CartesianData(Number x, Number y, Map<String, Object> properties) {
        this.x = x;
        this.y = y;
        this.properties = properties;
    }

    public Number getX() {
        return x;
    }

    public void setX(Number x) {
        this.x = x;
    }

    public Number getY() {
        return y;
    }

    public void setY(Number y) {
        this.y = y;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
