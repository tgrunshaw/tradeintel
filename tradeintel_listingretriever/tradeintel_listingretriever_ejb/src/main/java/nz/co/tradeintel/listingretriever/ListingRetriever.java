/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.tradeintel.listingretriever;

import java.io.IOException;
import java.io.StringReader;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import nz.co.tradeintel.listingretriever.databaseupdater.JAXBNewInstanceWrapper;
import nz.co.tradeintel.trademe.ListedItemDetailRequest;
import nz.co.tradeintel.trademe.TradeMeApiCallerBeanRemote;
import nz.co.tradeintel.trademe.exceptions.ApiRestrictionException;
import nz.co.tradeintel.trademe.exceptions.HttpFailureException;
import nz.co.tradeintel.trademe.exceptions.InvalidListingException;
import nz.co.tradeintel.trademe.exceptions.NoFreshCredentialsException;
import nz.co.tradeintel.trademe.exceptions.OAuthCheckedException;
import nz.co.tradeintel.trademe.oauth.TradeMeApiCredentials;
import nz.co.tradeintel.trademe.oauth.TradeMeApiCredentialsEAORemote;
import nz.co.trademe.api.custom.ErrorResult;
import nz.co.trademe.api.v1.ListedItemDetail;

/**
 * This class is used to retrieve a single listing from TradeMe using the API
 * credential type specified, but just returns the item and does not store in
 * the database. This is used by the ListingDetailUpdaterMessageBean & anything
 * else that wants to retrieve a listing. Latest revision separates the getting
 * of the xml & parsing into an entity into two separate steps so that Solr can
 * index the listing in the middle.
 *
 * @author Tim
 * @version 03.07.13
 */
@Stateless
public class ListingRetriever implements ListingRetrieverRemote, ListingRetrieverLocal {

    @EJB
    private TradeMeApiCallerBeanRemote apiCaller;
    @EJB
    private JAXBNewInstanceWrapper jaxbWrapper;
    
    @EJB
    private TradeMeApiCredentialsEAORemote apiCredentialEAO;
    
    private TradeMeApiCredentials kevsApiCredential;
    
    @PostConstruct
    public void init(){
        kevsApiCredential = apiCredentialEAO.getTradeMeApiCredential("04E29651BDB4802CF06FAA52A18A325129");
    }
    
    /**
     * Defaults to using Kevs token and just not increment the numberofcallsused column.
     * Instead it waits for ApiRestrictionException to be thrown. This was added Feb 2014 to 
     * stop so many database writes being used. It doesn't need to check because TradeMe doesn't restrict
     * kevs credential if we max it out ever hour (I assume). 
     */
    @Override
    public String getListing(int listingId) throws ApiRestrictionException, InvalidListingException, IOException{
        return getListing(listingId, kevsApiCredential);
    }

    /**
     * Get a ListedItemDetail from trademe for the given listingId using any
     * credential that matches the usedBy parameter. Throws
     * {@link ApiRestrictionException} if there are no more calls left of the
     * credentials. Throws a {@link InvalidListingException} if the listing is
     * invalid (TradeMe returns a ErrorResult as opposed to a ListedItemDetail).
     *
     */
    @Override
    public String getListing(int listingId, TradeMeApiCredentials.UsedBy usedBy)
            throws NoFreshCredentialsException, IOException, InvalidListingException {

        ListedItemDetailRequest listingDetailSearch = new ListedItemDetailRequest(listingId);

        // Get the listingDetailXml (throws HttpFailureException if Trademe returns server error. 
        //See: http://developer.trademe.co.nz/forum/?mingleforumaction=viewtopic&t=244.0)
        String listingDetailXml = null;
        try {
            listingDetailXml = apiCaller.send(listingDetailSearch, usedBy);
            return listingDetailXml;
        } catch (HttpFailureException httpEx) {
            // See if the response was a server error.
            // See this for trademe repsonses: http://developer.trademe.co.nz/api-overview/error-reporting/
            String responseBody = httpEx.getBody();
            try {
                Unmarshaller unmarshaller = jaxbWrapper.getJc().createUnmarshaller(); // 25uS to create.
                JAXBElement<ErrorResult> errorResultElement = unmarshaller.unmarshal(new StreamSource(new StringReader(httpEx.getBody())), ErrorResult.class);
                ErrorResult errorResult = errorResultElement.getValue();
                //oauth_problem=Unknown%20or%20previously%20rejected%20token%20%22F4A2FD1623AB14418A14981C0C8CB0F47A%22&oauth_problem_advice=The%20request%20token%20must%20be%20valid
                if (errorResult.getErrorDescription().startsWith("oauth_problem=Unknown%20or%20previously%20rejected%20token")) {
                    throw new OAuthCheckedException(errorResult.getErrorDescription());
                } else {
                    // Find the error and put the listing in the InvalidListing table. 
                    throw new InvalidListingException(errorResult.getErrorDescription());
                }
            } catch (JAXBException jaxbEx) {
                // An unknown server exception has occured with TradeMe.
                // Remove the throw httpEx & uncomment below if we want to continue trying listings after getting this error. 
                throw httpEx;
                /*
                 * System.err.println("HttpFailureException - Unable to
                 * determine cause of failure for listing: " + listingId);
                 * InvalidListing expiredListing = new InvalidListing();
                 * expiredListing.setListingId(listingId);
                 * expiredListing.setListingError("HTTP_FAILURE_JAXB_ERROR_PARSING_LISTING");
                 * em.merge(expiredListing);
                 * throw new
                 * InvalidListingException("HTTP_FAILURE_JAXB_ERROR_PARSING_LISTING");
                 */
            }
        }
    }

    @Override
    public String getListing(int listingId, TradeMeApiCredentials credential)
            throws ApiRestrictionException, OAuthCheckedException, IOException, InvalidListingException {

        ListedItemDetailRequest listingDetailSearch = new ListedItemDetailRequest(listingId);

        // Get the listingDetailXml (throws HttpFailureException if Trademe returns server error. 
        //See: http://developer.trademe.co.nz/forum/?mingleforumaction=viewtopic&t=244.0)
        String listingDetailXml = null;
        try {
            listingDetailXml = apiCaller.send(listingDetailSearch, credential);
            return listingDetailXml;
        }catch(ApiRestrictionException apiEx){
            throw apiEx;
        } catch (HttpFailureException httpEx) {
            // See if the response was a server error.
            // See this for trademe repsonses: http://developer.trademe.co.nz/api-overview/error-reporting/
            String responseBody = httpEx.getBody();
            try {
                Unmarshaller unmarshaller = jaxbWrapper.getJc().createUnmarshaller(); // 25uS to create.
                JAXBElement<ErrorResult> errorResultElement = unmarshaller.unmarshal(new StreamSource(new StringReader(httpEx.getBody())), ErrorResult.class);
                ErrorResult errorResult = errorResultElement.getValue();
                //oauth_problem=Unknown%20or%20previously%20rejected%20token%20%22F4A2FD1623AB14418A14981C0C8CB0F47A%22&oauth_problem_advice=The%20request%20token%20must%20be%20valid
                if (errorResult.getErrorDescription().startsWith("oauth_problem=Unknown%20or%20previously%20rejected%20token")) {
                    throw new OAuthCheckedException(errorResult.getErrorDescription());
                } else {
                    // Find the error and put the listing in the InvalidListing table. 
                    throw new InvalidListingException(errorResult.getErrorDescription());
                }

            } catch (JAXBException jaxbEx) {
                // An unknown server exception has occured with TradeMe.
                // Remove the throw httpEx & uncomment below if we want to continue trying listings after getting this error. 
                throw httpEx;
                /*
                 * System.err.println("HttpFailureException - Unable to
                 * determine cause of failure for listing: " + listingId);
                 * InvalidListing expiredListing = new InvalidListing();
                 * expiredListing.setListingId(listingId);
                 * expiredListing.setListingError("HTTP_FAILURE_JAXB_ERROR_PARSING_LISTING");
                 * em.merge(expiredListing);
                 * throw new
                 * InvalidListingException("HTTP_FAILURE_JAXB_ERROR_PARSING_LISTING");
                 */
            }
        }
    }

    @Override
    public ListedItemDetail parseListing(String listingDetailXml) throws JAXBException, InvalidListingException {

        Unmarshaller unmarshaller = jaxbWrapper.getJc().createUnmarshaller(); // 25uS to create.

        //unmarshaller.setEventHandler(new DefaultValidationEventHandler()); //USEFUL
        /*
         * Try to parse the listingDetailXML.
         */
        JAXBElement<ListedItemDetail> listedItemElement = null;
        try {
            // Parse the listingDetailXml (throws JAXBException & ClassCastException)
            listedItemElement = unmarshaller.unmarshal(new StreamSource(new StringReader(listingDetailXml)), ListedItemDetail.class);
        } catch (JAXBException | ClassCastException e) {

            // Check for ClassCastException, but don't kill a production server (with assertions off). 
            assert !(e instanceof ClassCastException) : "listingDetailXml is not a ListedItemDetail - it is only ever expected to be this.";

            // Throw JAXB Exception
            throw new InvalidListingException("JAXB_ERROR_PARSING_LISTING");
        }
        // Finally if no exceptions were thrown, return the listedItemDetail.
        return listedItemElement.getValue();
    }

    @Override
    public ListedItemDetail parseListingDebug(String listingDetailXml) throws JAXBException, ClassCastException {
        Unmarshaller unmarshaller = jaxbWrapper.getJc().createUnmarshaller(); // 25uS to create.
        unmarshaller.setEventHandler(new DefaultValidationEventHandler()); //USEFUL
        /*
         * Try to parse the listingDetailXML.
         */
        JAXBElement<ListedItemDetail> listedItemElement = null;
        // Parse the listingDetailXml (throws JAXBException & ClassCastException)
        listedItemElement = unmarshaller.unmarshal(new StreamSource(new StringReader(listingDetailXml)), ListedItemDetail.class);
        // Finally if no exceptions were thrown, return the listedItemDetail.
        return listedItemElement.getValue();
    }

    @Override
    public ListedItemDetail getAndParseListing(int listingId, TradeMeApiCredentials.UsedBy usedBy)
            throws OAuthCheckedException, JAXBException, NoFreshCredentialsException, IOException, InvalidListingException {
        return parseListing(getListing(listingId, usedBy));
    }

    @Override
    public ListedItemDetail getAndParseListing(int listingId, TradeMeApiCredentials credential)
            throws OAuthCheckedException, JAXBException, NoFreshCredentialsException, IOException, InvalidListingException {
        return parseListing(getListing(listingId, credential));
    }
}
