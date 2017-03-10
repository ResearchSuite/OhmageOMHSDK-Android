package edu.cornell.tech.foundry.ohmclient;

/**
 * Created by jameskizer on 2/4/17.
 */
public class OMHClientSignInResponse {

    private String accessToken;
    private String refreshToken;

    public OMHClientSignInResponse(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
