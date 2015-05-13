/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.tradeintel.listingretriever.databaseupdater;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import nz.co.trademe.api.v1.ListedItemDetail;

/**
 * A thread safe EAO Bean for ListedItemDetail. There is an overlapping class
 * in Tradeintel_web. The two classes were created to minimise the communication
 * between the EARs which was considered to be a source of significant
 * performance
 * degradation. Consequently, both classes duplicate some functionality. Check
 * the other class before implementing new functionality.
 *
 * @author Tim
 * @version 26.11.12
 */
@Startup // Needed to fix a weird issue involving: 'Singleton is unavailable because its original initialization failed.'
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) // So that database updates are commited immediately.
@Lock(LockType.WRITE) // All methods have a write lock.
@Singleton
public class ListedItemDetailEAOBean implements ListedItemDetailEAO, ListedItemDetailEAOLocal {

    @PersistenceContext(unitName = "trademe_data")
    private EntityManager em;

    @Override
    public void merge(ListedItemDetail lid) {
        if (em.contains(lid)) {
            /**
             * Haven't tested how slow this is.
             * If we merge a listedItemDetail that already exists, it will
             * auto-generate new QuestionPages and new BidPages, Bidpages_bid, 
             * and associated bids which will then all be orphaned.
             */
            throw new IllegalArgumentException("Cannot add a listeditemdetail "
                    + "that already exists! For listing: " + lid.getListingId());
        }
        em.merge(lid); // Typically 1 - 2 milliseconds to merge. 
    }

    @Override
    public void merge(InvalidListing invalidListing) {
        em.merge(invalidListing);
    }

    /*
     * Created so that it is easy to remove a listing if you add it only as a
     * test.
     * E.g. say you manually add a listing that has not expired, you will want
     * to
     * remove it to allow it to be readded by the sceduluer.
     *
     */
    @Override
    public String removeListing(int listingId) {
        ListedItemDetail item = em.find(ListedItemDetail.class, listingId);

        if (item != null) {
            em.remove(item);
            return "ListedItemDetail with listingid " + listingId + " successfully removed!";
        } else {
            return "Could not find ListedItemDetail with listingId: " + listingId;
        }
    }

    @Override
    public ListedItemDetail getListing(int listingID) {
        ListedItemDetail l = em.find(ListedItemDetail.class, listingID);
        return l;
    }

    @Override
    public List<Integer> getAllListingIDs() {
        String queryString = "SELECT l.listingId FROM ListedItemDetail l";

        Query query = em.createQuery(queryString);
        return (List<Integer>) query.getResultList();
    }

    @Override
    public List<ListedItemDetail> getListings(Collection<Integer> listingIDs) {
        if (listingIDs.isEmpty()) {
            return Collections.<ListedItemDetail>emptyList();
        }
        Query query = em.createNamedQuery("getAllListingsWithId");
        query.setParameter("listingIds", listingIDs);
        List<ListedItemDetail> listings = query.getResultList();
        return listings;
    }

    @Override
    public List<ListedItemDetail> getListingsEagerly(Collection<Integer> listingIDs) {
        if (listingIDs.isEmpty()) {
            return Collections.<ListedItemDetail>emptyList();
        }
        Query query = em.createNamedQuery("eagerlyGetAllListingsWithId");
        query.setParameter("listingIds", listingIDs);
//        query.setParameter("listingIds", listingIDs);
//        query.setHint(QueryHints.BATCH, )
//        Server c = em.unwrap(Server.class);
//
//        ClassDescriptor cd = c.getClassDescriptor(ListedItemDetail.class);
//        FetchGroupManager fgm = cd.getFetchGroupManager();
//        FetchGroup group = fgm.createFullFetchGroup();
//        em.unwrap(Server.class).getClassDescriptor(ListedItemDetail.class).getFetchGroupManager().createFullFetchGroup();
//        query.setHint(QueryHints.FETCH_GROUP, group);
        List<ListedItemDetail> listings = query.getResultList();
        return listings;
    }

    @Override
    public List<ListedItemDetail> getLatestListings(int numberToRetrieve) {
        Query q = em.createNamedQuery("getLastestListings");
        q.setMaxResults(numberToRetrieve);
        List<ListedItemDetail> listings = q.getResultList();
        return listings;
    }
}
