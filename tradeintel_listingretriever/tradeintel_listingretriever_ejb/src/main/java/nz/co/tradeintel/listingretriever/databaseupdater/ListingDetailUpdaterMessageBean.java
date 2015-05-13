/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.tradeintel.listingretriever.databaseupdater;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import nz.co.tradeintel.listingretriever.ListingRetrieverLocal;
import nz.co.tradeintel.listingretriever.solr.SolrManager;
import nz.co.tradeintel.trademe.exceptions.ApiRestrictionException;
import nz.co.tradeintel.trademe.exceptions.HttpFailureException;
import nz.co.tradeintel.trademe.exceptions.InvalidListingException;
import nz.co.tradeintel.trademe.exceptions.NoFreshCredentialsException;
import nz.co.tradeintel.trademe.exceptions.OAuthCheckedException;
import nz.co.tradeintel.trademe.util.TradeMeErrorDescription;
import nz.co.trademe.api.v1.ListedItemDetail;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * These beans are delegated work to do via sending them:
 * messageProducer.send(message); where message is a listingId in String form.
 * They are designed to be run concurrently, and run all database merge
 * operations through the singleton beans that manage (by blocking) concurrent
 * merge operations.
 *
 * On an 'intermittent' failure such as NoFreshCredentials, HttpFailure, etc.
 * This thread will sleep until the next hour then retry. It will do this
 * indefinitely. Note: Thread.sleep() is frowned upon in JAVA EE, but probably
 * the best option here.
 *
 * @author Tim
 * @version 26.11.12
 */
@MessageDriven(mappedName = "jms/listing", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ListingDetailUpdaterMessageBean implements MessageListener {

    @EJB
    private ListedItemDetailEAOLocal concurrentEAO;
    @EJB
    private ResetApiCredentialsBean resetApiCredsBean;
    @EJB
    private ListingRetrieverLocal listingRetriever;
    @EJB
    private SolrManager solrUpdater;

    @Override
    public void onMessage(Message inMessage) {
        try {
            if (inMessage instanceof TextMessage) {
                // Check that message is indeed a TextMessage.
                TextMessage message = (TextMessage) inMessage;
                int listingid = Integer.valueOf(message.getText());
                int temp_unavailable_retry_number = 0;

                // Put in infinite loop so that we can keep trying to get the listing after sleeping until the next hour.
                while (true) {
                    try {
                        String listedItemDetailXml = listingRetriever.getListing(listingid);
                        ListedItemDetail listedItemDetail = listingRetriever.parseListing(listedItemDetailXml);

                        // If the auction has expired
                        if (listedItemDetail.getAsAt().after(listedItemDetail.getEndDate())) {
                            try {
                                concurrentEAO.merge(listedItemDetail);
                            } catch (Throwable t) {
                                // This should be temporary, but I haven't checked if it actually works yet. In theory
                                // the outer try-catch should be identical. 
                                System.out.println("Exception for listingid: " + listingid);
                                throw t;
                            }
                            solrUpdater.updateSolr(listedItemDetailXml);
                        } else {
                            DateTime endDateTime = new DateTime(listedItemDetail.getEndDate());
                            DateTime startDateTime = new DateTime(listedItemDetail.getStartDate());
                            Days listingDuration = Days.daysBetween(startDateTime, endDateTime);

                            /*
                             * If the listing hasn't ended yet, but it's
                             * duration is
                             * greater than 14 days, then just continue. Some
                             * auctions
                             * (such as Services) has a duration of 3 months
                             * which would
                             * make everything else out of date.
                             * Cars & DVD's have auction lengths of 14 days.
                             */
                            if (listingDuration.isGreaterThan(Days.days(14))) {
                                // Do nothing.
                            } else {
                                logWarning("Listing " + listedItemDetail.getListingId() + " had an ASAT date after its ENDDATE"
                                        + "\nThis will try to get the listing again each hour, indefinitely.");
                                sleepUntilNextHour();
                                continue;
                            }
                        }
                        break; // Do not want to continue while. Only exceptions continue.
                    } catch (ApiRestrictionException apiEx) {
                        // Do nothing, we've just run out of credentials.
                        sleepUntilNextHour(1000); // Offset by 1 second.
                        continue;
                    } catch (NoFreshCredentialsException noFreshEx) {
                        // Do nothing, we've just run out of credentials.
                        sleepUntilNextHour(1000); // Offset by 1 second.
                        resetApiCredsBean.resetCredentialsCount();  // The first thread will reset, the rest will block, 
                        // then return as they have already been reset.
                        continue;
                    } catch (InvalidListingException invLEx) {
                        switch (TradeMeErrorDescription.getError(invLEx.getMessage())) {
                            case SERVER_ERROR:
                                logWarning("SERVER_ERROR: TradeMe returned a 500 - Internal Server Error for Listing: " + listingid);
                                InvalidListing serverErrorListing = new InvalidListing();
                                serverErrorListing.setListingId(listingid);
                                serverErrorListing.setListingError(TradeMeErrorDescription.SERVER_ERROR.toString());
                                concurrentEAO.merge(serverErrorListing);
                                break;
                            case CLASSIFIED_EXPIRED:
                                InvalidListing expiredListing = new InvalidListing();
                                expiredListing.setListingId(listingid);
                                expiredListing.setListingError(TradeMeErrorDescription.CLASSIFIED_EXPIRED.toString());
                                concurrentEAO.merge(expiredListing);
                                break;
                            case INVALID_LISTING:
                                InvalidListing notValidListing = new InvalidListing();
                                notValidListing.setListingId(listingid);
                                notValidListing.setListingError(TradeMeErrorDescription.INVALID_LISTING.toString());
                                concurrentEAO.merge(notValidListing);
                                break;
                            case ADMIN_REMOVED:
                                InvalidListing adminRemovedListing = new InvalidListing();
                                adminRemovedListing.setListingId(listingid);
                                adminRemovedListing.setListingError(TradeMeErrorDescription.ADMIN_REMOVED.toString());
                                concurrentEAO.merge(adminRemovedListing);
                                break;
                            case NO_LONGER_AVAILABLE:
                                InvalidListing noAvListing = new InvalidListing();
                                noAvListing.setListingId(listingid);
                                noAvListing.setListingError(TradeMeErrorDescription.NO_LONGER_AVAILABLE.toString());
                                concurrentEAO.merge(noAvListing);
                                break;
                            case TEMPORARILY_UNAVAILABLE:
                                temp_unavailable_retry_number++;
                                if (temp_unavailable_retry_number <= 6) {
                                    // Keep trying for 6 hours
                                    sleepUntilNextHour(1000);
                                    continue;
                                } else {
                                    InvalidListing tempUnav = new InvalidListing();
                                    tempUnav.setListingId(listingid);
                                    tempUnav.setListingError(TradeMeErrorDescription.TEMPORARILY_UNAVAILABLE.toString());
                                    concurrentEAO.merge(tempUnav);
                                    break;
                                }
                            default:
                                if ("JAXB_ERROR_PARSING_LISTING".equals(invLEx.getMessage())) {
                                    // This is a custom one thrown if JAXB cannot parse it.
                                    // TradeMe does not send this error.
                                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                                            "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                                                Thread.currentThread().getStackTrace()[2].getMethodName(),
                                                "Error Parsing Response of Listing: " + listingid});
                                    InvalidListing jaxBListing = new InvalidListing();
                                    jaxBListing.setListingId(listingid);
                                    jaxBListing.setListingError("JAXB_ERROR_PARSING_LISTING");
                                    concurrentEAO.merge(jaxBListing);
                                    break;
                                } else {
                                    logWarning("HttpFailure - Not an API_CALL_EXCEEDED reponse, "
                                            + "SERVER_ERROR, CLASSIFIED_EXPIRED or INVALID_LISTING error."
                                            + "\nError occured on listing id: " + listingid);
                                    sleepUntilNextHour();
                                    continue;
                                }
                        }
                    } catch (OAuthCheckedException oaEx) {
                        logWarning("Receieved an OAuthException while updating listings forward. Exception message was: " + oaEx.getMessage());
                        sleepUntilNextHour();
                        continue;
                    } catch (HttpFailureException httpEx) {
                        // Unknown httpFailure has occurred. Try to give as much info as possible.
                        logWarning("\nAn unknown HttpFailure Exception occured. (Usually server 400/500 error code)"
                                + "\nException occured on listing id: " + listingid
                                + "\nResponse code: " + httpEx.getCode()
                                + "\nResponse body: " + httpEx.getBody());
                        sleepUntilNextHour();
                        continue;
                    }
                    break; // This is reached if an InvalidListingException has been thrown. 
                }
            } else {
                Logger.getLogger(ListingDetailUpdaterMessageBean.class.getName()).log(Level.SEVERE,
                        "Message is not TextMessage in: ListingDetailUpdaterMessageBean");
            }
        } catch (Throwable t1) {
            // Used to catch any error & display the listingId
            int listingid = 0;
            try {
                if (inMessage instanceof TextMessage) {
                    // Check that message is indeed a TextMessage.
                    TextMessage message = (TextMessage) inMessage;
                    listingid = Integer.valueOf(message.getText());
                }
            } catch (Throwable t2) {
                Logger.getLogger(ListingDetailUpdaterMessageBean.class.getName()).log(Level.SEVERE, "Could not determine listingid error occurred on", t1);
                return;
            }
            Logger.getLogger(ListingDetailUpdaterMessageBean.class.getName()).log(Level.SEVERE, "For listingID: " + listingid, t1);
            return;
        }
    }

    /**
     * Offset is the time in milliseconds to sleep past the hour. Useful for
     * resetting the apiCredentials when it may be off by a few milliseconds.
     *
     * @param offset
     * @throws InterruptedException
     */
    private void sleepUntilNextHour(int offset) throws InterruptedException {
        Date now = new Date();
        Date nextHour = DateUtils.ceiling(now, Calendar.HOUR);
        long timeToSleepTillNextHour = nextHour.getTime() - now.getTime() + offset;
        Thread.sleep(timeToSleepTillNextHour);
    }

    private void sleepUntilNextHour() throws InterruptedException {
        sleepUntilNextHour(0);
    }

    private void logWarning(String message) {
        Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                    Thread.currentThread().getStackTrace()[2].getMethodName(),
                    message});
    }
}
