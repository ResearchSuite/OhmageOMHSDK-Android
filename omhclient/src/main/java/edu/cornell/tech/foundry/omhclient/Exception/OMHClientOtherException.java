package edu.cornell.tech.foundry.omhclient.Exception;

/**
 * Created by jameskizer on 2/5/17.
 */
public class OMHClientOtherException extends OMHClientException {

    private Exception underlyingError;

    public OMHClientOtherException(Exception underlyingError) {
        this.underlyingError = underlyingError;
    }

    public Exception getUnderlyingError() {
        return underlyingError;
    }

}
