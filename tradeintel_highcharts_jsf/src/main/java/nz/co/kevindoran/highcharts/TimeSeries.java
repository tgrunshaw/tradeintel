package nz.co.kevindoran.highcharts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author kevin
 */
public class TimeSeries {
   
    private ArrayList<TimeData> data = new ArrayList<>();
    
    public TimeSeries() {
    }
    
    public void addData(Date dateTime, Number number) {
        TimeData td = new TimeData(dateTime, number);
        data.add(td);
    }
    
    public void addData(Date dateTime, Number number, Map<String, Object> properties) {
        TimeData td = new TimeData(dateTime, number, properties);
        data.add(td);
    }
    
    public void removeAll() {
        data = new ArrayList<>();
    }
    
    public List<TimeData> getData() {
        return data;
    }
    

    
        /*
    // The LinkedHashMap maintains the data order (the order that it is inserted).
    public Map<Date, Number> data = new LinkedHashMap<>();
    
    public Map<Date, Number> getData() {
        return data;
    }
    
    public List<Entry<Date, Number>> getDataInListForm() {
        Entry<Date, Number> e;
        e.
        List<Entry<Date, Number>> listForm = new ArrayList<>(data.entrySet());
        return listForm;
    }
    
    public List<Date> getDateTimes() {
        Set<Date> times = data.keySet();
        List<Date> listForm = new ArrayList<>(times);
        return listForm;
    }
    
    public List<Number> getValues() {
        Collection<Number> values =  data.values();
        List<Number> listForm = new ArrayList<>(values);
        return listForm;
    }
    
    public void addAll(Map<Date, Number> data) {
        data.putAll(data);
    }
    
    public void add(Date date, Number number) {
        data.put(date, number);
    }
    
    public void removeAll() {
        data = new LinkedHashMap<>();
    }*/
}
