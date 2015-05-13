package nz.col.priceowl.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import nz.co.tradeintel.listingsearch.AutoSearchGenerator;
import nz.co.tradeintel.listingsearch.ItemSample;
import nz.co.tradeintel.listingsearch.ListingSearch;
import nz.co.tradeintel.listingsearch.ListingSearcher;
import nz.co.tradeintel.trademe.exceptions.InvalidListingException;
import nz.co.tradeintel.trademe.exceptions.NoFreshCredentialsException;
import nz.co.tradeintel.trademe.oauth.TradeMeApiCredentials;
import nz.co.tradeintel.trademe.util.ListingIdExtractor;
import nz.co.tradeintel.users.UseLimiter;
import nz.co.tradeintel.users.User;
import nz.co.tradeintel.users.UserDetails;
import nz.co.tradeintel.users.UserManagerRemote;
import nz.co.tradeintel.web.WebUserIdentity;
import nz.co.trademe.api.v1.Item;
import nz.co.trademe.api.v1.ListedItemDetail;

/**
 * @author Kevin
 */
@Named
@RequestScoped
public class AuctionResearcher {

	@EJB
	UserManagerRemote userManager;
	@EJB(beanName = "SolrSearcher")
	private ListingSearcher searcher;
	@EJB
	private UseLimiter useLimiter;
	private final AutoSearchGenerator searchGenerator
		= new AutoSearchGenerator();

	private ListedItemDetail searchedAuction;
	private boolean isSearchComplete = false;
	private String auctionNoOrUrl = "";
	private AuctionStats stats;
	private TradeMeApiCredentials credential;
	private double priceDifference;
	private boolean isBuyer = true;
	private boolean isNegative = false;
	private boolean wasSold = true;

	public String getAuctionNoOrUrl() {
		return auctionNoOrUrl;
	}

	public void setAuctionNoOrUrl(String auctionNoOrUrl) {
		this.auctionNoOrUrl = auctionNoOrUrl;
	}

	public boolean isSearchComplete() {
		return isSearchComplete;
	}

	public ListedItemDetail getSearchedAuction() {
		return searchedAuction;
	}

	public AuctionStats getStats() {
		return stats;
	}

	private TradeMeApiCredentials getCredentials() {
		if (credential == null) {
			credential = userManager.getUser(
				"tgrunshaw@gmail.com")
				.getUserDetails().getApiCredential();
		}
		return credential;
	}

	public void calculate() {
		if (recordAndCheckUse()) {
			return;
		}
		try {
			searchAndProcess(getCredentials());
		} catch (NoFreshCredentialsException noFreshCredEx) {
			// Switch to Kev's credential if Tim's fails.
			Logger.getLogger(AuctionResearcher.class.getName()).log(
				Level.WARNING,
				"No fresh credentials for user: "
				+ credential.getEmailAddress()
				+ "\nAttempting to use another credential.");

			try {
				User user = userManager.getUser("k.a.doran1@gmail.com");
				UserDetails userDetails = user.getUserDetails();
				credential
					= userDetails.getApiCredential();
				searchAndProcess(credential);
			} catch (IOException | JAXBException |
				NoFreshCredentialsException ex) {
				// IOException: If TradeMe is down / we can't connect to it.      
				displayRetrievalErrorMessage();
			} catch (InvalidListingException ex) {
				displayInvalidListingMessage();
			}
		} catch (IOException | JAXBException ex) {
			displayRetrievalErrorMessage();
		} catch (InvalidListingException | NumberFormatException ex) {
			// NumberFormatException can be thrown when trying to parse the
			// auction URL/number.
			displayInvalidListingMessage();
		}
	}

	public void compare(boolean isBuyer) {
		this.isBuyer = isBuyer;
		calculate();
		BigDecimal actualPrice = searchedAuction.getMaxBidAmount();
		if (actualPrice == null) {
			wasSold = false;
		} else {
			wasSold = true;
			priceDifference = stats.getItemStats().getMeanPrice() - actualPrice.doubleValue();
			if (priceDifference < 0) {
				isNegative = true;
				priceDifference = -priceDifference;
			}
		}
	}

	public boolean isWasSold() {
		return wasSold;
	}

	public double getPriceDifference() {
		return priceDifference;
	}

	public boolean isIsBuyer() {
		return isBuyer;
	}

	public boolean isIsNegative() {
		return isNegative;
	}

	public String getFormattedValue() {
		String formatted = NumberFormat.getCurrencyInstance().format(stats.getItemStats().getMeanPrice());
		return formatted;
	}

//	public void compare() {
//		try {
//			int auctionNumber = ListingIdExtractor.extract(auctionNoOrUrl);
//			ListedItemDetail lid = ListingDownloader.download(auctionNumber, getCredentials());
//			if (lid != null) {
//				BigDecimal actual = lid.getMaxBidAmount();
//				
//			}
//			else {
//				throw new IOException();
//			}
//		} catch (NumberFormatException | InvalidListingException ex) {
//			displayInvalidListingMessage();
//		} catch (IOException | JAXBException | NoFreshCredentialsException ex) {
//			displayRetrievalErrorMessage();
//		}
//	}
	private void searchAndProcess(TradeMeApiCredentials credential) throws
		NoFreshCredentialsException, IOException, JAXBException,
		InvalidListingException {
		int auctionNumber = ListingIdExtractor.extract(auctionNoOrUrl);
		ListingSearch search = searchGenerator.generate(auctionNumber,
			credential);
		searchedAuction = searchGenerator.getItem();
		ItemSample<Item> results = searcher.getItemSample(search);
		stats = new AuctionStats(results);
		isSearchComplete = true;
	}

	private void displayInvalidListingMessage() {
		FacesContext.getCurrentInstance().
			addMessage(null, new FacesMessage("The auction appears to"
					+ " be invalid. Please enter a valid auction ID or URL"));
	}

	private void displayRetrievalErrorMessage() {
		FacesContext.getCurrentInstance().
			addMessage(null, new FacesMessage("Unable to retrieve"
					+ " the details of that auction."));
	}

	private void display() {
		FacesContext.getCurrentInstance().
			addMessage(null, new FacesMessage("Unable to retrieve"
					+ " the details of that auction."));
	}

	private boolean recordAndCheckUse() {
		HttpServletRequest request
			= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		WebUserIdentity userIdentity = new WebUserIdentity(request);
		String ipAddress = userIdentity.getIpAddress();
		String uuid = userIdentity.getUuid();
		useLimiter.recordRequest(ipAddress, uuid);
		boolean isLimitReached = useLimiter.isUserLimitReached(uuid)
			|| useLimiter.isIPLimitReached(ipAddress);
		if (isLimitReached) {
			System.err.println("Limited reached for: " + ipAddress);
			FacesContext.getCurrentInstance().
				addMessage(null, new FacesMessage(
						"Unfortunately, you have reached your "
						+ "search limit of 5 requests "
						+ "per day. You will be able to "
						+ "search again tomorrow."));
		}
		return isLimitReached;
	}
}
