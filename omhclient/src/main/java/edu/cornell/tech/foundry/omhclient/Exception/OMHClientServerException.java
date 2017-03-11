package edu.cornell.tech.foundry.omhclient.Exception;

/**
 * Created by jameskizer on 2/5/17.
 */
public class OMHClientServerException extends OMHClientException {

    private String error;
    private String errorMessage;

    public OMHClientServerException(String error, String errorMessage) {
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public String getError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
