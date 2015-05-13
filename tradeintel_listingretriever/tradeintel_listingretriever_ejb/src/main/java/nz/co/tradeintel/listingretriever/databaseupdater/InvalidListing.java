package nz.co.tradeintel.listingretriever.databaseupdater;

import java.io.Serializable;
import javax.persistence.*;



/**
 * Entity implementation class for Entity: InvalidListing
 *
 */
@Entity
public class InvalidListing implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	@Id
	private int listingId;
    private String listingError;

	public InvalidListing() {
		super();
	}

    /**
     * @return the listingId
     */
    public int getListingId() {
        return listingId;
    }

    /**
     * @param listingId the listingId to set
     */
    public void setListingId(int listingId) {
        this.listingId = listingId;
    }

    /**
     * @return the listingError
     */
    public String getListingError() {
        return listingError;
    }

    /**
     * @param listingError the listingError to set
     */
    public void setListingError(String listingError) {
        this.listingError = listingError;
    }
   
}
