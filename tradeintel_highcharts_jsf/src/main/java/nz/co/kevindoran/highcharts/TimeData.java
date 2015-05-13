package nz.co.kevindoran.highcharts;


import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

    public class TimeData {
        private Date dateTime;
        private Number value;
        // For some reason, using Collections.EMPTY_MAP causes an error to be
        // thrown. Instead, a fragment is used in the .xmhtml to test for null.
        // I could assign it a new empty collection, but I fear that this could
        // be computationally and memory wasteful (say there are 5000 results, 
        // there will be 5000 empty maps).
        private Map<String, Object> properties; 
       
        public TimeData(Date dateTime, Number number) {
            this(dateTime, number, Collections.<String, Object>emptyMap());
        }
        
        public TimeData(Date dateTime, Number number, Map<String, Object> properties) {
            this.dateTime = dateTime;
            this.value = number;
            this.properties = properties;
        }
        
        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
        
        public Date getDateTime() {
            return dateTime;
        }

        public void setDateTime(Date dateTime) {
            this.dateTime = dateTime;
        }

        public Number getValue() {
            return value;
        }

        public void setValue(Number number) {
            this.value = number;
        }
    }