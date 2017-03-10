package edu.cornell.tech.foundry.ohmclient.Exception;

/**
 * Created by jameskizer on 2/5/17.
 */
public class OMHClientCredentialsFailure extends OMHClientException {

    private String description;

    public OMHClientCredentialsFailure(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
