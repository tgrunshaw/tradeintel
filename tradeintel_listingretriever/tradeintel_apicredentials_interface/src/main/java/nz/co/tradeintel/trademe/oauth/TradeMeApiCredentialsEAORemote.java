/**
 *
 */
package nz.co.tradeintel.trademe.oauth;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import java.io.IOException;
import java.util.List;
import javax.ejb.Remote;
import javax.persistence.EntityManager;
import nz.co.tradeintel.trademe.exceptions.HttpFailureException;
import org.joda.time.Duration;

/**
 * @author Tim
 * @version 15.11.13
 *
 */
@Remote
public interface TradeMeApiCredentialsEAORemote {

    // The extra minute is a buffer to insure the API limit has been removed before the credentials are used again.
    public static final Duration API_TIMEOUT = Duration.standardMinutes(60 + 1);

    public TradeMeApiCredentials getTradeMeApiCredential(String accessToken);

    /**
     * This method simply adds a credential to the database without any checking
     * that the key that matches the same user (identified by a uuid) that
     * requested the OAuth token. Use addKey() instead to enforce this check.
     *
     * Called after the user is redirected back to TradeIntel's callback page.
     * Returns the accesstoken associated with the credential.
     */
    public String addKeyUnsafe(String temporaryToken, String verifierString)
            throws InvalidRequestToken, HttpFailureException, IOException;

    /**
     * Adds a apiCredential to the database. Pass the expected uuid (a
     * previously generated salted hash stored in the user table for example) of
     * the user you are about to grant authorisation to. This method will ensure
     * that it matches the same uuid that was used to generate the request. This
     * counters the following attack:
     *
     * Victim requests OAuth access. A redirect url is generated for them.
     *
     * They are redirected and click 'allow' on Trademe. They are then
     * redirected back to TradeIntel, BUT... an attacker (say over wifi)
     * captures their return url with the oauth parameters, blocks it, and
     * enters it on their browser so that the server responds to a request from
     * their browser.
     *
     * Without a uuid check, the server will blindly add this token and the
     * application will link it to the logged in user.
     *
     * @throws InvalidRequestToken - If the token or uuid is invalid.
     * @throws HttpFailureException
     * @throws IOException
     */
    public String addKey(String temporaryToken, String verifierString, String expectedUuid)
            throws InvalidRequestToken, HttpFailureException, IOException;

    /**
     * Updates the nickname, firstname, lastname, & email of all ApiCredentials.
     *
     * @throws HttpFailureException
     * @throws IOException
     */
    public void updateAllCredentialMemberDetails()
            throws HttpFailureException, IOException;

    /**
     * Get a list of all fresh (i.e. their hourly limit has not been reached &
     * has < 999 calls used) {@link TradeMeApiCredentials}, that are allowed to
     * be used by the {@link UsedBy} list passed as a parameter. @see
     * List\<ApiCredentials\> getAllFreshCredentials(ApiCredentials.UsedBy
     * usedBy)
     *
     * @param usedByList
     * @return
     */
    public List<TradeMeApiCredentials> getAllFreshCredentials(
            List<TradeMeApiCredentials.UsedBy> usedByList);

    /**
     * Get a list of all fresh (i.e. their hourly limit has not been reached &
     * has < 999 calls used) {@link TradeMeApiCredentials}, that are allowed to
     * be used by the single {@link UsedBy} passed as a parameter. @param usedBy
     * - A {@link UsedBy} enum specifying what ApiCredentials to use. @return
     */
    public List<TradeMeApiCredentials> getAllFreshCredentials(
            TradeMeApiCredentials.UsedBy usedBy);

    /**
     * Get a single fresh (i.e. their hourly limit has not been reached & has <
     * 999 calls used) {@link TradeMeApiCredentials}, that are allowed to be
     * used by the single {@link UsedBy} passed as a parameter. @param usedBy -
     * A {@link UsedBy} enum specifying what ApiCredentials to use. @return
     * <code>null</code> if no fresh credentials
     *
     * wer e found.
     */
    public TradeMeApiCredentials getFreshCredentials(
            TradeMeApiCredentials.UsedBy usedBy);

    /**
     * Get a single fresh (i.e. their hourly limit has not been reached & has <
     * 999 calls used) {@link TradeMeApiCredentials} matching {@link UsedBy}
     * NO_RESTRICTION. @return <code>null</code> if no fresh credentials were
     * found.
     */
    public TradeMeApiCredentials getFreshUnrestrictedCredentials();

    /**
     * Get a single fresh (i.e. their hourly limit has not been reached & has <
     * 999 calls used) {@link TradeMeApiCredentials}, that are allowed to be
     * used by the {@link UsedBy} list passed as a parameter. @param usedByList
     * @return <code>null</code> if no fresh credentials were found.
     */
    public TradeMeApiCredentials getFreshCredentials(
            List<TradeMeApiCredentials.UsedBy> usedByList);

    /**
     * Generates a keyRequestUrl to direct the user to. As this method does not
     * take a uuid, only addKeyUnsafe() can be used to add an apicredential.
     * Called after a button such as "Let TradeIntel use your account" is
     * pressed. Then the user is redirected to trademe to login.
     */
    public String generateKeyRequestUrl();

    /**
     * Override the default callback url. Called after a button such as "Let
     * TradeIntel use your account" is pressed. Then the user is redirected to
     * trademe to login.
     */
    public String generateKeyRequestUrl(String callbackUrl);

    /**
     * Generates a keyRequestUrl to direct the user to and maps the uuid to the
     * temporary token generated. When addKey() is later called, use this uuid
     * to ensure that the key is being added for the same user that this
     * generateKeyRequestUrl() was called for. F
     *
     * @param callbackUrl
     * @param uuid
     * @return
     */
    public String generateKeyRequestUrl(String callbackUrl, String uuid);

    /**
     * Call this method to reset all the credentials - but it will only reset
     * them if it is a new hour since they were last reset.
     */
    public void resetCredentialIfNewHour();

    public EntityManager getEntityManager();

    /**
     * Remove the TradeMeApiCredential referenced by the String accessToken
     * primary key. Returns true if the credential existed and was removed, or
     * false if it did not exist. Throws
     * MySQLIntegrityConstraintViolationException - Usually because more than
     * one TradeIntel user is using this token (same TradeMe account), and one
     * of them is changes their trademe account - trying to remove the shared
     * credential. This is wrapped as follows:
     *
     * -class javax.ejb.EJBException
     *
     * --class javax.transaction.RollbackException
     *
     * ---class org.eclipse.persistence.exceptions.DatabaseException
     *
     * ----class
     * com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
     */
    public boolean removeCredential(String accessToken);
}