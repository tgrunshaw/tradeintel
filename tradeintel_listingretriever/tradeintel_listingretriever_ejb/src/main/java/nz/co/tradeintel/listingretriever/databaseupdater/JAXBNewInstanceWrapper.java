/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.tradeintel.listingretriever.databaseupdater;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * The purpose of this bean is hold a JAXBContext. It does this by calling
 * JAXBContext.newInstance() in the constructor.
 * JAXBContext.newInstance() takes over 1.5 seconds to initialise so is not well
 * suited to be done in the message beans. 
 * JAXBContext is thread safe. Unmarshaller, however, isn't. (But takes ~25uS to create).
 * 
 * @author Tim
 * @version 26.11.12
 */
@Singleton
@LocalBean
public class JAXBNewInstanceWrapper {

    private final JAXBContext jc;
    // Needed for marshalling.
    private final String namespace = "http://api.trademe.co.nz/v1";

    public JAXBContext getJc() {
        return jc;
    }
     
     public JAXBNewInstanceWrapper() throws JAXBException{
         jc = JAXBContext.newInstance("nz.co.trademe.api.v1:nz.co.trademe.api.custom"); // Takes 1.5seconds to create instance!
     }

    public String getNamespace() {
        return namespace;
    }
}
