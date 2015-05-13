package nz.co.tradeintel.listingretriever.databaseupdater;

import java.util.Calendar;
import java.util.Date;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import nz.co.tradeintel.trademe.oauth.TradeMeApiCredentialsEAORemote;
import org.apache.commons.lang3.time.DateUtils;

/**
 * @author Tim
 * @version 26.11.12
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) // So that database updates are commited immediately.
@Lock(LockType.WRITE) // All methods have a write lock.
public class ResetApiCredentialsBean {
    
    @EJB TradeMeApiCredentialsEAORemote apiEAO;

    private Date lastHourReset = new Date();
    
    /*
     * We now call this from the actual message beans themselves.
     * If this is called concurrently, the first will reset the credentials and 
     * the other threads will wait, then upon testing they will return immediately as
     * the credentials have already been reset.
     * We keep the schedule in as it does no harm, and resets the credentials if there
     * aren't message beans pooled.
     */
    @Schedule(minute="00", hour="*", info="Reset Credential Counts")
    public void resetCredentialsCount(){
        Date nowHour = DateUtils.truncate(new Date(), Calendar.HOUR);
        if(nowHour.after(lastHourReset)){
            // Has yet to be run this hour.
            apiEAO.resetCredentialIfNewHour();
            lastHourReset = new Date();
        } // else just return.
    }
}
