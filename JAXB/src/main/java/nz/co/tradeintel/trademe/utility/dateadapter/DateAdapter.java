package nz.co.tradeintel.trademe.utility.dateadapter;

import java.sql.Timestamp;
import org.joda.time.DateTime;
import javax.xml.bind.DatatypeConverter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateAdapter {
    private static final DateTimeFormatter trademeAndSolrDateFormat = DateTimeFormat
            .forPattern("yyyy-MM-dd'T'H:m:s'Z'");
    
    public static Timestamp parseDateTime(String s) {
        return new Timestamp(DatatypeConverter.parseDateTime(s).getTime().getTime());
    }
    public static String printDateTime(Timestamp dt) {
        String timeFormatted = trademeAndSolrDateFormat.print(new DateTime(dt.getTime()));
        return timeFormatted;
    }
}