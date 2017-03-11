package edu.cornell.tech.foundry.omhclient.Exception;

/**
 * Created by jameskizer on 2/5/17.
 */
public class OMHClientUnreachable extends OMHClientException {

    private Exception underlyingError;

    public OMHClientUnreachable(Exception underlyingError) {
        this.underlyingError = underlyingError;
    }

    public Exception getUnderlyingError() {
        return underlyingError;
    }
}
