/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.tradeintel.listingretriever.databaseupdater;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import nz.co.tradeintel.listingretriever.solr.SolrManager;
import org.apache.solr.client.solrj.SolrServerException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Removes auctions when TradeIntel is no longer permitted to keep them. The
 * most
 * important method, removeOldAuctions, operates under two 'modes' - forced and
 * not forced. When in not-forced mode, it will do nothing if removing more than
 * the LIMIT_AUCTIONS_TO_AUTO_REMOVE value so that we don't auto delete the
 * database
 * due to a system time change.
 *
 */
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class OldAuctionRemoverBean implements OldAuctionRemover {

    /*
     * 44 days because we only remove once a day, so want to be safe and delete 
     * the next day as well. If it is 45 days, we show items that are not
     * publically available.
     */
    private static final int OLD_THRESHOLD_DAYS = 44;
    /*
     * If there are => than 1 millions auctions to be removed, the
     * removeOldAuctions() method will return requiring it to be manually run.
     * This is to ensure the db isn't deleted in the event that the system time
     * changes etc.
     */
    static final int LIMIT_AUCTIONS_TO_AUTO_REMOVE = 999999; // Approx 3 days & less than the solr limit of 1million

    /**
     * Enables the scheduled removal task.
     */
    private boolean isScheduledRemovalEnabled = false;

    /**
     * Enables the forcedRemoval task (which is scheduled every minute so that
     * it
     * runs in a none-http thread).
     */
    private boolean isForceRemovalEnabled = false;

    /**
     * Boolean that is set true while running so that it isn't called
     * concurrently.
     */
    private boolean isRunning = false;

    @EJB
    SolrManager solrManager;
    @EJB
    private AuctionRemover auctionRemover;
    @PersistenceContext(unitName = "trademe_data")
    private EntityManager em;

    @Override
    public void forceRemove() throws SolrServerException, IOException {
        isForceRemovalEnabled = true; // So that runs in another thread.
        System.out.println("Force remove called, this will run within a minute.");
    }

    @Override
    public boolean isSchedulerEnabled() {
        return isScheduledRemovalEnabled;
    }

    @Override
    public void stopAndDisable() {
        auctionRemover.stop();
        this.isForceRemovalEnabled = false;
        this.isScheduledRemovalEnabled = false;
    }

    @Override
    public void setSchedulerEnabled(boolean isEnabled) {
        System.out.println("Auction remover scheduler has been set to:" + isEnabled);
        this.isScheduledRemovalEnabled = isEnabled;
    }

    @Schedule(hour = "00", minute = "30")
    private void scheduledRemoval() throws SolrServerException, IOException {
        if (!isScheduledRemovalEnabled) {
            return;
        }
        if (isRunning) {
            System.out.println("scheduleRemoval called but removeOldAuctions is "
                    + "already running. This call will exit.");
            return;
        }
        // Do not force
        removeOldAuctions(false);
    }

    /**
     * Scheduled so that it runs in another thread.
     */
    @Schedule(second = "0", minute = "*", hour = "*")
    private void forceRemoval() throws SolrServerException, IOException {
        if (!isForceRemovalEnabled) {
            return;
        }
        if (isRunning) {
//            System.out.println("forceRemval called but removeOldAuctions is "
//                    + "already running. This call will exit.");
            return;
        }
        // Force
        removeOldAuctions(true);
        isForceRemovalEnabled = false;
    }

    /**
     * Called by both scheduled and forced removal.
     */
    private void removeOldAuctions(boolean force) throws SolrServerException, IOException {
        isRunning = true;
        try {
            System.out.println("removeOldAuctions run at " + new Date()
                    + " removing auctions older than " + OLD_THRESHOLD_DAYS + " days.");
            if (force) {
                System.out.println("Running in forced mode.");
            } else {
                System.out.println("Running in normal (non-forced) mode.");
            }

            // Get listings from solr
            List<Integer> toRemove
                    = solrManager.findAuctionsOlderThan(OLD_THRESHOLD_DAYS);
//            List<Integer> toRemove = findAuctionsOlderThanFromSQL(OLD_THRESHOLD_DAYS);

            // Test of we have maxed out the number of solr results we can return.
            boolean stillBatching = false;
            if (toRemove.size() == SolrManager.MAX_RESULT_SET_SIZE) {
                System.out.println("Maxed out the number of listing ids that can be "
                        + "retrieved from solr: " + SolrManager.MAX_RESULT_SET_SIZE + " listings.");
                if (force) {
                    System.out.println("In force mode - going to batch delete by re-running this method.");
                    stillBatching = true;
                } else {
                    /**
                     * Do not run if in normal mode. If MAX_RESULT_SET_SIZE <
                     * LIMIT_AUCTIONS_TO_AUTO_REMOVE,
                     * the batching will just override the
                     * LIMIT_AUCTIONS_TO_AUTO_REMOVE value. So easiest
                     * to just require the user run in forced mode.
                     */
                    System.err.println("In normal mode - going to exit. Please run (carefully) in force mode.");
                    return;
                }

            }

            // Return if over limit of auctions to remove and not in force mode.
            if ((toRemove.size() > LIMIT_AUCTIONS_TO_AUTO_REMOVE) && !force) {
                System.err.println("removeOldAuctions exiting!");
                System.err.println("There were " + toRemove.size() + " auctions "
                        + "older than " + OLD_THRESHOLD_DAYS + " days, which is above"
                        + "the limit of " + LIMIT_AUCTIONS_TO_AUTO_REMOVE);
                System.err.println("Please run manually.");
                return;
            }

            // Start actually removing
            System.out.println("Removing " + toRemove.size() + " auctions.");
            System.out.println("Removing from database and solr...");
            try {
                auctionRemover.removeAuctions(toRemove);
                while (stillBatching) {
                    // If we've hit the limit of results we can return from solr.
                    assert force : "We should never be batching if not running in force mode.";
                    System.out.println("Running another solr batch...");
                    solrManager.commitAllOutstandingUpdates();
                    toRemove = solrManager.findAuctionsOlderThan(OLD_THRESHOLD_DAYS);
                    if (toRemove.size() != SolrManager.MAX_RESULT_SET_SIZE) {
                        stillBatching = false;
                    }
                    auctionRemover.removeAuctions(toRemove);
                }

                // Remove invalid auctions
                System.out.println("Removing invalid listings...");
                Query oldestListingIdQuery = em.createNativeQuery("SELECT MIN(listingid) FROM listeditemdetail;").setMaxResults(1);
                Object o = oldestListingIdQuery.getSingleResult();
                if (o != null) {
                    int oldestListingId = (int) o;
                    auctionRemover.removeInvalidListingsLessThan(oldestListingId);
                }
            } catch (SolrServerException solrEx) {
                throw solrEx;
            } catch (Exception ex) {
                Logger.getLogger(OldAuctionRemoverBean.class.getName()).log(Level.SEVERE, null, ex);
                // The solr logger sometimes has issues printing. Include a system out just incase.
                System.err.println("Exception: " + ex.getMessage());
                return;
            }

            System.out.println("Successfully removed all old auctions.");
        } finally {
            isRunning = false;
        }
    }
    
    
    private List<Integer> findAuctionsOlderThanFromSQL(int days){
        DateTime earliestEndDate = DateTime.now().minusDays(days).withTimeAtStartOfDay();
        
        Timestamp ts = new Timestamp(earliestEndDate.getMillis()); // haven't tested if accounts for timezone
        System.out.println("Timestamp: " + ts);        
        
        
        Query oldAuctionsQuery = em.createNativeQuery("SELECT listingid FROM listeditemdetail where enddate < ? LIMIT 0,1000000;")
                .setParameter(1, ts.toString());
        List<Integer> results = oldAuctionsQuery.getResultList();
        
        System.out.println("Size: " + results.size());
        //System.out.println("E.G.: " + results.get(0) + " or " + results.get(456999));
        
        return results;
    }
}
