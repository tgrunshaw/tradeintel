package nz.co.tradeintel.listingretriever.databaseupdater;

import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import nz.co.trademe.api.v1.ListedItemDetail;

/**
 *
 * Each of the methods (addExpiredListingsForwards/Backwards) runs indefinitely  
 * by setting the appropriate boolean flag to true. Listings are then added to 
 * the queue by the @Scheduled addListings method that runs every hour. 
 * You want the queue to be topped up so that it never empties, so for a 
 * Schedule of one hour, you expect that the queue (100,000) will not empty in 
 * 1 hour.
 * fillInGaps() just adds up to MAX_QUEUE_LENGTH items to the queue then quits.
 *
 * Useful stat: A pool size of 20 gave a listing retrieval rate of ~68 listings
 * per second on limoncello.
 *
 * @author Tim
 * @version 29.11.12
 */
@Singleton
@TransactionManagement(TransactionManagementType.BEAN) // Can only send 1000 messages per transaction, so need to start new ones.
public class ListingDetailUpdaterBean implements ListingDetailUpdater {

    @Resource(mappedName = "jms/listingConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/listing")
    private Queue queue;
    @PersistenceContext(unitName = "trademe_data")
    private EntityManager em;
    // Error if we choose over 100,000 listings to add.
    //Caused by: com.sun.messaging.jmq.jmsserver.util.BrokerException: [B4120]: Can not add message 100001-192.168.1.4(b6:6b:75:87:9d:58)-1-1353903598418 to destination listingPhysicalQueue [Queue]. The destination message count limit (maxNumMsgs) of 100000 has been reached.
    private static final int MAX_QUEUE_LENGTH = 20000;//99000; Make 20,000 then we use pretty much all on an hour & there isn't a huge lag (~5hrs) if we addSingleListing to the queue.  // Actually 100,000 but this gives us some leeway. 
    // The maximum number of messages a message producer can send in a single transaction.
    private static final int imq_transaction_producer_maxNumMsgs = 1000;
    @Resource
    UserTransaction ut;
    Connection connection;
    Session session;
    MessageProducer messageProducer;
    TextMessage message;
    boolean addExpiredListingsForward = false;
    boolean addExpiredListingsBackward = false;

    @PostConstruct
    private void postConstruct() {
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(true, 0); // 'When you create a session in an enterprise bean, the container ignores the arguments you
            //  specify, because it manages all transactional properties for enterprise beans. It is still a good idea
            //  to specify arguments of true and 0 to the createSession method to make this situation clear.'
            messageProducer = session.createProducer(queue);
            messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);   // Messages are not persistent (i.e. written to disk). This is good as we do not want 
            // lots of old messages to be reloaded after a server crash for instance. 

            message = session.createTextMessage();
        } catch (JMSException ex) {
            Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns: [queueDepth, lastListingId]
     */
    private int[] getQueueDepthAndLastListingId() throws Exception {
        try {
            ut.begin();
            int queueDepth = 0;
            QueueBrowser qb = session.createBrowser(queue);
            Enumeration messagesOnQueue = qb.getEnumeration();
            Object item = null;
            int lastListingId = 0;
            while (messagesOnQueue.hasMoreElements()) {
                item = messagesOnQueue.nextElement();
                queueDepth++;
            }
            qb.close(); // Fixes memory leak issue.
            if (queueDepth > 0) {
                TextMessage textMessage = (TextMessage) item;
                String finalListingIdString = textMessage.getText();
                lastListingId = Integer.parseInt(finalListingIdString);
            }
            ut.commit();
            return new int[]{queueDepth, lastListingId};
        } catch (Exception ex) {
            try {
                if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    ut.rollback();
                }
            } catch (SystemException ex1) {
                Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex1);
            }
            throw ex;
        }
    }

    @Schedule(minute="15", hour="*", info="Adding listings to queue")
    private void addListings() {
        // If either are running then schedule run this method.
        if (addExpiredListingsForward || addExpiredListingsBackward) {
            try {
                int[] qd_listId = getQueueDepthAndLastListingId();
                int queueDepth = qd_listId[0];
                int lastListingId;
                int numListingsToAddToQueue = MAX_QUEUE_LENGTH - queueDepth;

                Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                        "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            "Adding another " + numListingsToAddToQueue + " listings to the queue at " + new Date()});

                /**
                 * ******** Forwards ********
                 */
                if (addExpiredListingsForward) {
                    if (queueDepth > 0) {
                        lastListingId = qd_listId[1];
                    } else {
                        // Find newest listingId from database.
                        lastListingId = getNewestListingId();
                        if (lastListingId == 0) {
                            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                                    "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                                        "No listings in the database!"});
                            return;
                        }
                    }
                    int newestListingId = lastListingId + 1;

                    ut.begin();
                    // Runs for the first time, and then every hour.
                    for (int i = 0; i < numListingsToAddToQueue; i++) {
                        if ((i % imq_transaction_producer_maxNumMsgs) == 0) {
                            // Every imq_transaction_producer_maxNumMsgs start a new transaction.
                            ut.commit();
                            ut.begin();
                        }
                        /*
                        Slow down adding messages so that we don't max out disk.
                        This will add 20/second, or 20*3600 per hour. 
                        */
                        Thread.sleep(50);
                        message.setText(String.valueOf(newestListingId + i));
                        messageProducer.send(message);
                    }
                    ut.commit();
                } 
                /**
                 * ******** Backwards ********
                 */
                else if (addExpiredListingsBackward) {
                    if (queueDepth > 0) {
                        lastListingId = qd_listId[1];
                    } else {
                        lastListingId = getOldestListingId();
                        if (lastListingId == 0) {
                            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                                    "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                                        "No listings in the database!"});
                            return;
                        }
                    }
                    int oldestListingId = lastListingId - 1;

                    ut.begin();
                    // Runs for the first time, and then every hour.
                    for (int i = 0; i < numListingsToAddToQueue; i++) {
                        if ((i % imq_transaction_producer_maxNumMsgs) == 0) {
                            // Every imq_transaction_producer_maxNumMsgs start a new transaction.
                            ut.commit();
                            ut.begin();
                        }
                        message.setText(String.valueOf(oldestListingId - i));
                        messageProducer.send(message);
                    }
                    ut.commit();
                }
            } catch (Exception ex) {
                try {
                    if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        ut.rollback();
                    }
                } catch (SystemException ex1) {
                    Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex1);
                }
                Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void addExpiredListingsForward() {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        "addExpiredListingsForward run at: " + new Date()});

            addExpiredListingsBackward = false;
            addExpiredListingsForward = true;
    }
    
    
      /**
     * Finds the smallest ListingId in the listedItemDetail table, and retrieves
     * each ListedItemDetail with a smaller listingId from TradeMe. (Works
     * backwards in time)
     */
    @Override
    public void addExpiredListingsBackward() {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        "addExpiredListingsBackward run at: " + new Date()});
            
            addExpiredListingsForward = false;
            addExpiredListingsBackward = true;
    }

    /**
     * Used to add a single listing (good to start if database is empty). Note:
     * you need to add an expired listing and a valid listing to start adding
     * listings forward.
     */
    @Override
    public void addExpiredListingSingle(int listingId) {
        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        "addExpiredListingsSingle run at: " + new Date()});

            ut.begin();
            message.setText(String.valueOf(listingId));
            messageProducer.send(message);
            ut.commit();
        } catch (Exception ex) {
            try {
                if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    ut.rollback();
                }
            } catch (SystemException ex1) {
                Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @Override
    public void fillInGapsInExpiredListings() {
        try {
            int newestListingId = getNewestListingId();
            int oldestListingId = getOldestListingId();

            if (newestListingId == 0) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                        "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            "No listings in the database!"});
                return;
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    "{0} : {1} : {2}", new Object[]{this.getClass().getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        "fillInGapsExpiredListings run at: " + new Date()});

            ut.begin();
            int messageSentCount = 0; // Used to check we don't send more than 1000 messages.
            while ((newestListingId > oldestListingId) && (messageSentCount < MAX_QUEUE_LENGTH)) {
                newestListingId--;
                if (em.find(ListedItemDetail.class, newestListingId) == null) {
                    // Check to see if it is in the InvalidListing table. 
                    if (em.find(InvalidListing.class, newestListingId) == null) {
                        if ((messageSentCount % imq_transaction_producer_maxNumMsgs) == 0) {
                            ut.commit();
                            ut.begin();
                        }
                        message.setText(String.valueOf(newestListingId));
                        messageProducer.send(message);
                        messageSentCount++;
                    }
                }
            }
            ut.commit();
        } catch (JMSException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | NotSupportedException | SystemException ex) {
            try {
                if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    ut.rollback();
                }
            } catch (SystemException ex1) {
                Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Returns 0 if there are no listings in the database. Returns -1 if there
     * is an error.
     */
    private int getNewestListingId() {
        try {
            ut.begin();
            Query newestListingIdQuery;

            Query invalidListingCountQuery = em.createNativeQuery("SELECT COUNT(*) FROM invalidlisting").setMaxResults(1);
            Query listedItemDetailCountQuery = em.createNativeQuery("SELECT COUNT(*) FROM listeditemdetail").setMaxResults(1);
            long invalidListingCount = (long) invalidListingCountQuery.getSingleResult();
            long listedItemDetailCount = (long) listedItemDetailCountQuery.getSingleResult();

            if (invalidListingCount > 0 && listedItemDetailCount > 0) {
                newestListingIdQuery = em.createNativeQuery("SELECT GREATEST((SELECT MAX(listingid) FROM invalidlisting), (SELECT MAX(listingid) FROM listeditemdetail));").setMaxResults(1);
            } else if (invalidListingCount > 0) {
                newestListingIdQuery = em.createNativeQuery("SELECT MAX(listingid) FROM invalidlisting;").setMaxResults(1);
            } else if (listedItemDetailCount > 0) {
                newestListingIdQuery = em.createNativeQuery("SELECT MAX(listingid) FROM listeditemdetail;").setMaxResults(1);
            } else {
                ut.commit();
                return 0;
            }
            int newestListingId = (int) newestListingIdQuery.getSingleResult();
            ut.commit();
            return newestListingId;
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | NotSupportedException | SystemException ex) {
            try {
                if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    ut.rollback();
                }
            } catch (SystemException ex1) {
                Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex);

            return -1;
        }
    }

    private int getOldestListingId() {
        try {
            ut.begin();
            Query oldestListingIdQuery;

            Query invalidListingCountQuery = em.createNativeQuery("SELECT COUNT(*) FROM invalidlisting;").setMaxResults(1);
            Query listedItemDetailCountQuery = em.createNativeQuery("SELECT COUNT(*) FROM listeditemdetail;").setMaxResults(1);
            long invalidListingCount = (long) invalidListingCountQuery.getSingleResult();
            long listedItemDetailCount = (long) listedItemDetailCountQuery.getSingleResult();

            if (invalidListingCount > 0 && listedItemDetailCount > 0) {
                oldestListingIdQuery = em.createNativeQuery("SELECT LEAST((SELECT MIN(listingid) FROM invalidlisting), (SELECT MIN(listingid) FROM listeditemdetail));").setMaxResults(1);
            } else if (invalidListingCount > 0) {
                oldestListingIdQuery = em.createNativeQuery("SELECT MIN(listingid) FROM invalidlisting;").setMaxResults(1);
            } else if (listedItemDetailCount > 0) {
                oldestListingIdQuery = em.createNativeQuery("SELECT MIN(listingid) FROM listeditemdetail;").setMaxResults(1);
            } else {
                ut.commit();
                return 0;
            }
            int oldestListingId = (int) oldestListingIdQuery.getSingleResult();
            ut.commit();
            return oldestListingId;
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | NotSupportedException | SystemException ex) {
            try {
                if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    ut.rollback();
                }
            } catch (SystemException ex1) {
                Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex);

            return -1;
        }
    }

    @PreDestroy
    private void preDestroy() {
        try {
            connection.close();
        } catch (JMSException ex) {
            Logger.getLogger(ListingDetailUpdaterBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
