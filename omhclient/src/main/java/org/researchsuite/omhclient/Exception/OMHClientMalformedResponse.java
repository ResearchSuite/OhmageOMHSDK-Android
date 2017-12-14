package org.researchsuite.omhclient.Exception;

import org.json.JSONObject;

/**
 * Created by jameskizer on 2/5/17.
 */
public class OMHClientMalformedResponse extends OMHClientException {

    private String responseBody;

    public OMHClientMalformedResponse(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
