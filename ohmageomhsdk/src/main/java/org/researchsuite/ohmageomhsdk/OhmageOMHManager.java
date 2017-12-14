package org.researchsuite.ohmageomhsdk;


import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;

import org.researchsuite.ohmageomhsdk.Exceptions.OhmageOMHAlreadySignedIn;
import org.researchsuite.ohmageomhsdk.Exceptions.OhmageOMHInvalidSample;
import org.researchsuite.ohmageomhsdk.Exceptions.OhmageOMHNotSignedIn;
import org.researchsuite.omhclient.Exception.OMHClientDataPointConflict;
import org.researchsuite.omhclient.Exception.OMHClientInvalidAccessToken;
import org.researchsuite.omhclient.OMHClient;
import org.researchsuite.omhclient.OMHClientSignInResponse;
import org.researchsuite.omhclient.OMHDataPoint;

import java.io.File;
import java.io.IOException;

public class OhmageOMHManager implements ObjectQueue.Listener<String> {

    final static String TAG = OhmageOMHManager.class.getSimpleName();

    private static String ACCESS_TOKEN = "AccessToken";
    private static String REFRESH_TOKEN = "RefreshToken";

    private static OhmageOMHManager manager = null;
    private static Object managerLock = new Object();

    private OhmageOMHSDKCredentialStore credentialStore;
    private String accessToken;
    private String refreshToken;
    private Object credentialsLock;
    private boolean credentialStoreUnlocked;

    private Context context;

    private OMHClient client;

    private Object uploadLock;
    FileObjectQueue<String> ohmageOMHDatapointQueue;
    boolean isUploading = false;

    @Nullable
    public static OhmageOMHManager getInstance() {
        synchronized (managerLock) {
            return manager;
        }
    }

    public static void config(Context context, String baseURL, String clientID, String clientSecret, OhmageOMHSDKCredentialStore store, String queueStorageDirectory) {
        synchronized (managerLock) {
            if (manager == null) {
                manager = new OhmageOMHManager(context, baseURL, clientID, clientSecret, store, queueStorageDirectory);
            }
        }
    }

    @Nullable
    private String getAccessToken() {
        //if local accessToken is null, try to load
        if (this.accessToken == null) {
            byte[] accessTokenData = this.credentialStore.get(context, ACCESS_TOKEN);
            if (accessTokenData != null) {
                String accessToken = new String(accessTokenData);
                if (accessToken != null  && !accessToken.isEmpty()) {
                    this.accessToken = accessToken;
                }
            }
        }
        return this.accessToken;
    }

    @Nullable
    private String getRefreshToken() {
        //if local refreshToken is null, try to load
        if (this.refreshToken == null) {
            byte[] refreshTokenData = this.credentialStore.get(context, REFRESH_TOKEN);
            if (refreshTokenData != null) {
                String refreshToken = new String(refreshTokenData);
                if (refreshToken != null  && !refreshToken.isEmpty()) {
                    this.refreshToken = refreshToken;
                }
            }
        }
        return this.refreshToken;
    }

    private OhmageOMHManager(Context context, String baseURL, String clientID, String clientSecret, OhmageOMHSDKCredentialStore store, String queueStorageDirectory) {

        this.context = context;
        this.client = new OMHClient(baseURL, clientID, clientSecret);

        this.credentialsLock = new Object();

        this.credentialStore = store;

        this.credentialStoreUnlocked = false;
//        this.getAccessToken();
//        String savedAccessToken = this.getAccessToken();
//        if(savedAccessToken != null) {
//            this.accessToken = savedAccessToken;
//        }

//        this.getRefreshToken();
//        String savedRefreshToken = this.getRefreshToken();
//        if(savedRefreshToken != null) {
//            this.refreshToken = savedRefreshToken;
//        }

        //load queue from disk
        this.uploadLock = new Object();

        File queueFile = new File(context.getFilesDir() + queueStorageDirectory);
        OhmageOMHDatapointConverter converter = new OhmageOMHDatapointConverter();
        try {
            this.ohmageOMHDatapointQueue = new FileObjectQueue<>(queueFile, converter);
        } catch(IOException e) {
            e.printStackTrace();
        }

        //this calls onAdd for each element on queue
        this.ohmageOMHDatapointQueue.setListener(this);

        //try to upload any existing datapoints
//        this.upload();

    }

    public void setCredentialStoreUnlocked(boolean credentialStoreUnlocked) {
        if (credentialStoreUnlocked) {
            this.getAccessToken();
            this.getRefreshToken();
            this.upload();
        }
        this.credentialStoreUnlocked = credentialStoreUnlocked;
    }

    public boolean isSignedIn() {
        synchronized (this.credentialsLock) {
            return this.credentialStore.has(context, REFRESH_TOKEN);
        }
    }

    public Uri getAuthURI() {
        return this.client.getAuthURI();
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

        //clear queue as well
        while(this.ohmageOMHDatapointQueue.size() > 0) {
            this.ohmageOMHDatapointQueue.remove();
        }
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
            localRefreshToken = this.getRefreshToken();
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

    private void refresh(final Completion completion) {
        String localRefreshToken;
        synchronized (credentialsLock) {
            localRefreshToken = this.getRefreshToken();
        }

        client.refreshAccessToken(localRefreshToken, new OMHClient.AuthCompletion() {
            @Override
            public void onCompletion(OMHClientSignInResponse response, Exception e) {
                if (response != null && e == null) {
                    setCredentials(response.getAccessToken(), response.getRefreshToken());
                    completion.onCompletion(null);
                }
                else {
                    clearCredentials();
                    completion.onCompletion(new OhmageOMHNotSignedIn());
                }
            }
        });
    }

    private void tryToUpload() {

        assert(this.isSignedIn());
        assert(this.credentialStoreUnlocked);

        synchronized (this.uploadLock) {

            if (this.isUploading) {
                return;
            }

            if (this.ohmageOMHDatapointQueue.size() < 1) {
                return;
            }

            this.isUploading = true;

            String datapointString = this.ohmageOMHDatapointQueue.peek();

            assert(datapointString != null && !datapointString.isEmpty());

            String localAccessToken;
            synchronized (this.credentialsLock) {
                localAccessToken = this.getAccessToken();
            }

            assert(localAccessToken != null && !localAccessToken.isEmpty());

            this.client.postSample(datapointString, localAccessToken, new OMHClient.PostSampleCompletion() {
                @Override
                public void onCompletion(boolean success, Exception e) {

//                    OhmageOMHManager.this.isUploading = false;

                    if (success) {
                        Log.w(TAG, "Datapoint successfully uploaded");
                        OhmageOMHManager.this.ohmageOMHDatapointQueue.remove();

                        OhmageOMHManager.this.isUploading = false;
                        OhmageOMHManager.this.upload();
                        return;
                    }

                    if (e instanceof OMHClientInvalidAccessToken) {

                        Log.w(TAG, "Refreshing token");
                        refresh(new Completion() {
                            @Override
                            public void onCompletion(Exception e) {
                                if (e == null) {
                                    OhmageOMHManager.this.isUploading = false;
                                    OhmageOMHManager.this.upload();
                                }
                                else {
                                    OhmageOMHManager.this.clearCredentials();
                                    OhmageOMHManager.this.isUploading = false;
                                }
                            }
                        });
                        return;

                    }
                    else if (e instanceof OMHClientDataPointConflict){

                        OhmageOMHManager.this.ohmageOMHDatapointQueue.remove();
                        OhmageOMHManager.this.isUploading = false;
                        OhmageOMHManager.this.upload();
                        return;
                    }


                    else {
                        OhmageOMHManager.this.isUploading = false;
                        return;
                    }

                }
            });

        }
    }

    private void upload() {


        if (!this.credentialStoreUnlocked || !this.isSignedIn()) { return; }

        //start async task here

        class UploadTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {

                OhmageOMHManager.this.tryToUpload();

                return null;

            }
        }

        new UploadTask().execute();

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

        //add datapoint
        //this should notify the listener, which should start the upload
        String datapointString = datapoint.toJson().toString();
        this.ohmageOMHDatapointQueue.add(datapointString);

    }

    public void signIn(String username, String password, final Completion completion) {

        if (this.isSignedIn()) {
            completion.onCompletion(new OhmageOMHAlreadySignedIn());
            return;
        }

        assert(this.credentialStoreUnlocked);

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

    public void signIn(String code, final Completion completion) {

        if (this.isSignedIn()) {
            completion.onCompletion(new OhmageOMHAlreadySignedIn());
            return;
        }

        assert(this.credentialStoreUnlocked);

        this.client.signIn(code, new OMHClient.AuthCompletion() {
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


    //Queue Listener Methods
    @Override
    public void onAdd(ObjectQueue<String> queue, String entry) {
        this.upload();
    }

    @Override
    public void onRemove(ObjectQueue<String> queue) {

    }

}