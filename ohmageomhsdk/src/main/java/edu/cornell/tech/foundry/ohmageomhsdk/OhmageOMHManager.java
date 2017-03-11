package edu.cornell.tech.foundry.ohmageomhsdk;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import edu.cornell.tech.foundry.ohmageomhsdk.Exceptions.OhmageOMHAlreadySignedIn;
import edu.cornell.tech.foundry.ohmageomhsdk.Exceptions.OhmageOMHException;
import edu.cornell.tech.foundry.ohmageomhsdk.Exceptions.OhmageOMHInvalidSample;
import edu.cornell.tech.foundry.ohmageomhsdk.Exceptions.OhmageOMHNotSignedIn;
import edu.cornell.tech.foundry.omhclient.Exception.OMHClientInvalidAccessToken;
import edu.cornell.tech.foundry.omhclient.OMHClient;
import edu.cornell.tech.foundry.omhclient.OMHClientSignInResponse;
import edu.cornell.tech.foundry.omhclient.OMHDataPoint;

public class OhmageOMHManager {

    final static String TAG = OMHClient.class.getSimpleName();

    private static String ACCESS_TOKEN = "AccessToken";
    private static String REFRESH_TOKEN = "RefreshToken";

    private static OhmageOMHManager manager = null;
    private static Object managerLock = new Object();

    private OhmageOMHSDKCredentialStore credentialStore;
    private String accessToken;
    private String refreshToken;
    private Object credentialsLock;

    private Context context;

    private OMHClient client;

    @Nullable
    public static OhmageOMHManager getInstance() {
        synchronized (managerLock) {
            return manager;
        }
    }

    public static void config(Context context, String baseURL, String clientID, String clientSecret, OhmageOMHSDKCredentialStore store) {
        synchronized (managerLock) {
            if (manager == null) {
                manager = new OhmageOMHManager(context, baseURL, clientID, clientSecret, store);
            }
        }
    }

    @Nullable
    private String getAccessToken() {
        byte[] accessTokenData = this.credentialStore.get(context, ACCESS_TOKEN);
        if (accessTokenData != null) {
            String accessToken = new String(accessTokenData);
            if (accessToken != null  && !accessToken.isEmpty()) {
                return accessToken;
            }
        }

        return null;
    }

    @Nullable
    private String getRefreshToken() {
        byte[] refreshTokenData = this.credentialStore.get(context, REFRESH_TOKEN);
        if (refreshTokenData != null) {
            String refreshToken = new String(refreshTokenData);
            if (refreshToken != null  && !refreshToken.isEmpty()) {
                return refreshToken;
            }
        }

        return null;
    }

    private OhmageOMHManager(Context context, String baseURL, String clientID, String clientSecret, OhmageOMHSDKCredentialStore store) {

        this.context = context;
        this.client = new OMHClient(baseURL, clientID, clientSecret);

        this.credentialsLock = new Object();

        this.credentialStore = store;

        String savedAccessToken = this.getAccessToken();
        if(savedAccessToken != null) {
            this.accessToken = savedAccessToken;
        }

        String savedRefreshToken = this.getRefreshToken();
        if(savedRefreshToken != null) {
            this.refreshToken = savedRefreshToken;
        }

    }

    public boolean isSignedIn() {
        synchronized (this.credentialsLock) {
            return this.refreshToken != null && !this.refreshToken.isEmpty();
        }
    }

    private void setCredentials(String accessToken, String refreshToken) {
        synchronized (this.credentialsLock) {
            this.accessToken = accessToken;
            byte[] accessTokenData = accessToken.getBytes();
            this.credentialStore.set(context, ACCESS_TOKEN, accessTokenData);

            this.refreshToken = refreshToken;
            byte[] refreshTokenData = refreshToken.getBytes();
            this.credentialStore.set(context, REFRESH_TOKEN, refreshTokenData);
        }
    }

    private void clearCredentials() {
        synchronized (this.credentialsLock) {
            this.accessToken = null;
            this.credentialStore.remove(context, ACCESS_TOKEN);

            this.refreshToken = null;
            this.credentialStore.remove(context, REFRESH_TOKEN);
        }
    }



//    public static synchronized OhmageOMHManager getInstance() {
//        if (manager == null) {
//            manager = new OhmageOMHManager();
//        }
//        return manager;
//    }

    public interface Completion {
        void onCompletion(Exception e);
    }

    private void refreshThenAdd(final OMHDataPoint datapoint, final Completion completion) {
        String localRefreshToken;
        synchronized (credentialsLock) {
            localRefreshToken = refreshToken;
        }

        client.refreshAccessToken(localRefreshToken, new OMHClient.AuthCompletion() {
            @Override
            public void onCompletion(OMHClientSignInResponse response, Exception e) {
                if (response != null && e == null) {
                    setCredentials(response.getAccessToken(), response.getRefreshToken());
                    addDatapoint(datapoint, completion);
                }
                else {
                    clearCredentials();
                    completion.onCompletion(new OhmageOMHNotSignedIn());
                }
            }
        });
    }

    public void addDatapoint(final OMHDataPoint datapoint, final Completion completion) {

        if (!this.isSignedIn()) {
            completion.onCompletion(new OhmageOMHNotSignedIn());
            return;
        }

        if (!this.client.validateSample(datapoint)) {
            Log.w(TAG, "Dropping datapoint, it looks like it's invalid: " + datapoint.toJson().toString());
//            Log.w(TAG, datapoint);
            completion.onCompletion(new OhmageOMHInvalidSample());
            return;
        }

        String localAccessToken;
        synchronized (this.credentialsLock) {
            localAccessToken = this.accessToken;
        }

        this.client.postSample(datapoint.toJson(), localAccessToken, new OMHClient.PostSampleCompletion() {
            @Override
            public void onCompletion(boolean success, Exception e) {

                if (success) {
                    Log.w(TAG, "Datapoint successfully uploaded");
                    completion.onCompletion(null);
                    return;
                }

                if (e instanceof OMHClientInvalidAccessToken) {

                    Log.w(TAG, "Refreshing token");
                    refreshThenAdd(datapoint, completion);
                    return;

                }
                else {
                    completion.onCompletion(e);
                    return;
                }

            }
        });

    }

    public void signIn(String username, String password, final Completion completion) {

        if (this.isSignedIn()) {
            completion.onCompletion(new OhmageOMHAlreadySignedIn());
            return;
        }

        this.client.signIn(username, password, new OMHClient.AuthCompletion() {
            @Override
            public void onCompletion(OMHClientSignInResponse response, Exception e) {
                if (e != null) {
                    completion.onCompletion(e);
                    return;
                }

                if (response != null) {
                    setCredentials(response.getAccessToken(), response.getRefreshToken());
                }

                completion.onCompletion(null);
                return;
            }
        });

    }

    public void signOut(final Completion completion) {

        clearCredentials();
        completion.onCompletion(null);
    }

}