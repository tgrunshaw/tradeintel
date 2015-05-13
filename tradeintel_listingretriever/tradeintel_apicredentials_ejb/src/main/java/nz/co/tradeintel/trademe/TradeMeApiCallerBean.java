package nz.co.tradeintel.trademe;

import java.io.IOException;
import java.util.Date;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import nz.co.tradeintel.trademe.exceptions.ApiRestrictionException;
import nz.co.tradeintel.trademe.exceptions.NoFreshCredentialsException;
import nz.co.tradeintel.trademe.exceptions.OAuthCheckedException;
import nz.co.tradeintel.trademe.oauth.TradeMeApiCredentials;
import nz.co.tradeintel.trademe.oauth.TradeMeApiCredentialsConcurrentBeanLocal;
import nz.co.tradeintel.trademe.oauth.TradeMeOAuthRequestSender;

/**
 * Api calls should generally be made through this class, it manages finding
 * fresh credentials and change credentials seamlessly.
 *
 * @author Tim
 * @version 25.11.13
 */
@Stateless
public class TradeMeApiCallerBean implements TradeMeApiCallerBeanRemote {

    @EJB
    TradeMeApiCredentialsConcurrentBeanLocal credentialsConcurrentEAO;

    @Override
    public String send(TradeMeApiRequest request, TradeMeApiCredentials.UsedBy usedBy)
            throws NoFreshCredentialsException, IOException {
        TradeMeApiCredentials credentials = credentialsConcurrentEAO.getCredentialAndIncrementCountByOne(usedBy);
        if (credentials == null) {
            throw new NoFreshCredentialsException();
        }

        String response;
        // Keep trying to get a reponseBody with the credentials until it is either
        // successful or a NoFreshCredentialsException is thrown. 
        while (true) {
            try {
                response = send(request, credentials);
                break;
            } catch (ApiRestrictionException apiEx) {
                // This ApiRestrictionException is from TradeMe and due to a miscount / 
                // slight timing difference. Set the credentials as expired.
                credentials.setDateTimeLimitWasReached(new Date());
                credentialsConcurrentEAO.merge(credentials);

                // Get fresh credentials.
                credentials = credentialsConcurrentEAO.getCredentialAndIncrementCountByOne(usedBy);
                if (credentials == null) {
                    throw new NoFreshCredentialsException();
                }
                continue;
            }

        }
        return response;
    }

    @Override
    public String send(TradeMeApiRequest request, TradeMeApiCredentials credentials)
            throws OAuthCheckedException, ApiRestrictionException, IOException {
        TradeMeOAuthRequestSender requestSender = new TradeMeOAuthRequestSender(credentials);
        String responseBody = null;

        try {
            responseBody = request.send(requestSender);
        } catch (OAuthCheckedException oauthEx) {
            // The credential is not valid.
            throw oauthEx;
        }

        return responseBody;
    }
}
