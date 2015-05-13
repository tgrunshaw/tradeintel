/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.tradeintel.trademe.oauth;

import javax.ejb.Lock;
import javax.ejb.LockType;
import nz.co.tradeintel.trademe.exceptions.NoFreshCredentialsException;

/**
 *
 * @author Tim
 */
public interface TradeMeApiCredentialsConcurrentBeanLocal {

    // All retrieving of API Credentials is done through this.
    @Lock(value = LockType.WRITE)
    TradeMeApiCredentials getCredentialAndIncrementCountByOne(TradeMeApiCredentials.UsedBy usedBy) throws NoFreshCredentialsException;
    
    @Lock(value = LockType.WRITE)
    void incrementCountByOne(TradeMeApiCredentials credential);
    
    @Lock(value= LockType.WRITE)
    public void merge(TradeMeApiCredentials apiCredentials);
}
