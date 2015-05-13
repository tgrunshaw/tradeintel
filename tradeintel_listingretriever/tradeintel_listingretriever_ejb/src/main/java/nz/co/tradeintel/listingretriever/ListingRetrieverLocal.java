/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.tradeintel.listingretriever;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import nz.co.tradeintel.trademe.exceptions.InvalidListingException;
import nz.co.tradeintel.trademe.exceptions.NoFreshCredentialsException;
import nz.co.tradeintel.trademe.exceptions.OAuthCheckedException;
import nz.co.tradeintel.trademe.oauth.TradeMeApiCredentials;
import nz.co.trademe.api.v1.ListedItemDetail;

/**
 *
 * @author Tim
 */
public interface ListingRetrieverLocal {

    /**
     * Defaults to using Kevs token and just not increment the numberofcallsused column.
     * Instead it waits for ApiRestrictionException to be thrown. This was added Feb 2014 to 
     * stop so many database writes being used. It doesn't need to check because TradeMe doesn't restrict
     * kevs credential if we max it out ever hour (I assume). 
     */
    public String getListing(int listingId) throws NoFreshCredentialsException, InvalidListingException, IOException;
    /**
     * Get a ListedItemDetail from trademe for the given listingId using any
     * credential that matches the usedBy parameter. Throws
     * {@link ApiRestrictionException} if there are no more calls left of the
     * credentials. Throws a {@link InvalidListingException} if the listing is
     * invalid (TradeMe returns a ErrorResult as opposed to a ListedItemDetail).
     *
     */
    String getListing(int listingId, TradeMeApiCredentials.UsedBy usedBy) 
            throws OAuthCheckedException, NoFreshCredentialsException, IOException, InvalidListingException;

    /**
     * Get a ListedItemDetail from trademe for the given listingId using the
     * specified credential. Throws
     * {@link ApiRestrictionException} if there are no more calls left of the
     * credentials. Throws a {@link InvalidListingException} if the listing is
     * invalid (TradeMe returns a ErrorResult as opposed to a ListedItemDetail).
     *
     */
    String getListing(int listingId, TradeMeApiCredentials credential) 
            throws NoFreshCredentialsException, IOException, InvalidListingException;

    /**
     * Parse the XML retrieved from the getListing() method.
     */
    ListedItemDetail parseListing(String xml) throws JAXBException, InvalidListingException;

    /**
     * Same as parseListing(String xml), but turns on the default event handler
     * to get more error information.
     */
    ListedItemDetail parseListingDebug(String xml) throws JAXBException;

    /**
     * Combines the get() & parse() into one call.
     */
    ListedItemDetail getAndParseListing(int listingId, TradeMeApiCredentials.UsedBy usedBy) 
            throws OAuthCheckedException, JAXBException, NoFreshCredentialsException, IOException, InvalidListingException;

    /**
     * Combines the get() & parse() into one call.
     */
    ListedItemDetail getAndParseListing(int listingId, TradeMeApiCredentials credential)
            throws OAuthCheckedException, JAXBException, NoFreshCredentialsException, IOException, InvalidListingException;
}
