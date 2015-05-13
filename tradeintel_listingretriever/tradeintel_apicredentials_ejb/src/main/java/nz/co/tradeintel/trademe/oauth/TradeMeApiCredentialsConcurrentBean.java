/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.tradeintel.trademe.oauth;

import java.util.Date;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import nz.co.tradeintel.trademe.exceptions.NoFreshCredentialsException;

/**
 * Used to manage concurrent read & updating of api credentials.
 *
 * @author Tim
 * @version 26.11.12
 */
@Startup // Needed to fix a weird issue involving: 'Singleton is unavailable because its original initialization failed.'
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Lock(LockType.WRITE)
@Singleton
public class TradeMeApiCredentialsConcurrentBean implements TradeMeApiCredentialsConcurrentBeanLocal {

    @EJB
    private TradeMeApiCredentialsEAOBeanLocal apiCredsBean;

    // All retrieving of API Credentials is done through this.
    @Override
    public TradeMeApiCredentials getCredentialAndIncrementCountByOne(TradeMeApiCredentials.UsedBy usedBy) throws NoFreshCredentialsException {
        TradeMeApiCredentials apiCredentials = apiCredsBean.getFreshCredentials(usedBy);
        if (apiCredentials == null) {
            throw new NoFreshCredentialsException();
        }

        int numCallsUsed = apiCredentials.getNumberOfCallsUsed();

        apiCredentials.setNumberOfCallsUsed(numCallsUsed + 1);

        // If at 999, then set the time limit reached to be now.
        if (numCallsUsed == TradeMeApiCredentialsEAOBean.MAX_API_CALLS - 1) {
            apiCredentials.setDateTimeLimitWasReached(new Date()); // Close enough, it will be used soon. 
        }

        // Merge the updated apiCreds back into the database.
        apiCredsBean.getEntityManager().merge(apiCredentials);
        apiCredsBean.getEntityManager().detach(apiCredentials); // Just to be safe? Don't want this being managed anymore.

        return apiCredentials;
    }

    @Override
    public void merge(TradeMeApiCredentials apiCredentials) {
        apiCredsBean.getEntityManager().merge(apiCredentials);
        apiCredsBean.getEntityManager().flush();
    }

    @Override
    public void incrementCountByOne(TradeMeApiCredentials credential) {
        int numCallsUsed = credential.getNumberOfCallsUsed();
        credential.setNumberOfCallsUsed(numCallsUsed + 1);

        // If at 999, then set the time limit reached to be now.
        if (numCallsUsed == TradeMeApiCredentialsEAOBean.MAX_API_CALLS - 1) {
            credential.setDateTimeLimitWasReached(new Date()); // Close enough, it will be used soon. 
        }

        // Merge the updated apiCreds back into the database.
        apiCredsBean.getEntityManager().merge(credential);
    }
}
