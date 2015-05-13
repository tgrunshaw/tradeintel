package nz.co.trademe.api.custom;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Custom error description class, because trademe didn't include it in their schema. 
 * @author Tim
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ErrorResult", propOrder = {
        "request",
    "errorDescription"
})
@XmlRootElement(name="ErrorResult", namespace="http://api.trademe.co.nz/v1")
public class ErrorResult
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "Request", namespace="http://api.trademe.co.nz/v1")
    protected String request;
    @XmlElement(name = "ErrorDescription", namespace="http://api.trademe.co.nz/v1")
    protected String errorDescription;
    
    
    /**
     * @return the request
     */
    public String getRequest() {
        return request;
    }
    /**
     * @param request the request to set
     */
    public void setRequest(String request) {
        this.request = request;
    }
    /**
     * @return the errorDescription
     */
    public String getErrorDescription() {
        return errorDescription;
    }
    /**
     * @param errorDescription the errorDescription to set
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

}
