package org.researchsuite.omhclient;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;

import org.researchsuite.omhclient.Exception.OMHClientBadGateway;
import org.researchsuite.omhclient.Exception.OMHClientDataPointConflict;
import org.researchsuite.omhclient.Exception.OMHClientInvalidAccessToken;
import org.researchsuite.omhclient.Exception.OMHClientInvalidRefreshToken;
import org.researchsuite.omhclient.Exception.OMHClientMalformedResponse;
import org.researchsuite.omhclient.Exception.OMHClientOtherException;
import org.researchsuite.omhclient.Exception.OMHClientServerException;
import org.researchsuite.omhclient.Exception.OMHClientUnreachable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jameskizer on 2/4/17.
 */
public class OMHClient {

    final static String TAG = OMHClient.class.getSimpleName();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public interface AuthCompletion {
        void onCompletion(OMHClientSignInResponse response, Exception e);
    }

    public interface PostSampleCompletion {
        void onCompletion(boolean success, Exception e);
    }

    private final static OkHttpClient client = new OkHttpClient();

    private String baseURL;
    private String clientID;
    private String clientSecret;

    private String getBasicAuthString(String clientID, String clientSecret) {
        return Base64.encodeToString((clientID + ":" + clientSecret).getBytes(), Base64.NO_WRAP);
    }

    public OMHClient(String baseURL, String clientID, String clientSecret) {
        this.baseURL = baseURL;
        this.clientID = clientID;
        this.clientSecret = clientSecret;
    }

    public Uri getAuthURI() {
        StringBuilder sb = new StringBuilder(this.baseURL);
        sb.append("/oauth/authorize?client_id=");
        sb.append(this.clientID);
        sb.append("&response_type=code");
        return Uri.parse(sb.toString());
    }

    public void signIn(String username, String password, final AuthCompletion completion) {

        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("grant_type", "password")
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Basic " + this.getBasicAuthString(this.clientID, this.clientSecret))
                .url(this.baseURL + "/oauth/token")
                .post(body)
                .build();

        client.newCall(request).enqueue(this.processAuthResponse(completion));
    }

    public void signIn(String code, final AuthCompletion completion) {

        RequestBody body = new FormBody.Builder()
                .add("code", code)
                .add("grant_type", "authorization_code")
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Basic " + this.getBasicAuthString(this.clientID, this.clientSecret))
                .url(this.baseURL + "/oauth/token")
                .post(body)
                .build();

        client.newCall(request).enqueue(this.processAuthResponse(completion));
    }


    public void refreshAccessToken(String refreshToken, final AuthCompletion completion) {
        RequestBody body = new FormBody.Builder()
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Basic " + this.getBasicAuthString(this.clientID, this.clientSecret))
                .url(this.baseURL + "/oauth/token")
                .post(body)
                .build();

        client.newCall(request).enqueue(this.processAuthResponse(completion));
    }

    private Callback processAuthResponse(final AuthCompletion completion) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    completion.onCompletion(null, new OMHClientUnreachable(e));
                }
                else {
                    completion.onCompletion(null, new OMHClientOtherException(e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {

                    String responseBody = "";
                    try {
                        responseBody = response.body().string();
                        JSONObject responseJson = new JSONObject(responseBody);

                        String accessToken = responseJson.getString("access_token");
                        String refreshToken = responseJson.getString("refresh_token");

                        if (accessToken != null && refreshToken != null) {
                            completion.onCompletion(new OMHClientSignInResponse(refreshToken, accessToken), null);
                        }

                        else {
                            completion.onCompletion(null, null);
                        }

                    } catch (JSONException e) {
//                        Log.e(TAG, "Fail to parse response from omh-sign-in endpoint:" + responseBody, e);
                        completion.onCompletion(null, new OMHClientMalformedResponse(responseBody));
                        return;
                    }

                }
                else {

                    int responseCode = response.code();

                    if (responseCode == 502) {
                        completion.onCompletion(null, new OMHClientBadGateway());
                        return;
                    }
                    else {
                        String responseBody = "";
                        try {
                            responseBody = response.body().string();
                            JSONObject responseJson = new JSONObject(responseBody);

                            String error = responseJson.getString("error");
                            String errorDescription = responseJson.getString("error_description");

                            if (error != null && error.equals("invalid_grant")) {
                                completion.onCompletion(null, new OMHClientInvalidRefreshToken());
                            }
                            else if (error != null && errorDescription != null){
                                completion.onCompletion(null, new OMHClientServerException(error, errorDescription));
                            }
                            else {
                                completion.onCompletion(null, new OMHClientMalformedResponse(responseBody));
                            }

                        } catch (JSONException e) {
//                        Log.e(TAG, "Fail to parse response from omh-sign-in endpoint:" + responseBody, e);
                            completion.onCompletion(null, new OMHClientMalformedResponse(responseBody));
                            return;
                        }
                    }


                }


            }
        };
    }


    public boolean validateSample(OMHDataPoint sample) {
        boolean isValid = this.validateSampleJson(sample.toJson());
        return isValid;
    }

    public boolean validateSampleJson(JSONObject sampleJson) {
        try {
            String sampleJsonString = sampleJson.toString();
            Log.i(TAG, "validating json" + sampleJsonString);
            JSONObject recodedJson = new JSONObject(sampleJsonString);
            return true;
        } catch (JSONException ex) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void postSample(JSONObject sampleJson, String accessToken, final PostSampleCompletion completion) {

        this.postJSONSample(sampleJson, accessToken, completion);

    }

    public void postSample(String sampleString, String accessToken, final PostSampleCompletion completion) {

        this.postStringSample(sampleString, accessToken, completion);

    }

    private void postJSONSample(JSONObject sampleJson, String accessToken, final PostSampleCompletion completion) {

        String jsonString = sampleJson.toString();
        RequestBody body = RequestBody.create(JSON, jsonString);
        Request request = new Request.Builder()
                .url(this.baseURL + "/dataPoints")
                .header("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        client.newCall(request).enqueue(this.processJSONResponse(completion));
    }

    private void postStringSample(String jsonString, String accessToken, final PostSampleCompletion completion) {

        RequestBody body = RequestBody.create(JSON, jsonString);
        Request request = new Request.Builder()
                .url(this.baseURL + "/dataPoints")
                .header("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();

        client.newCall(request).enqueue(this.processJSONResponse(completion));
    }

    private Callback processJSONResponse(final PostSampleCompletion completion) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    completion.onCompletion(false, new OMHClientUnreachable(e));
                }
                else {
                    completion.onCompletion(false, new OMHClientOtherException(e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                int responseCode = response.code();

                if (response.isSuccessful() && responseCode == 201) {
                    completion.onCompletion(true, null);
                    return;
                }
                else if (responseCode == 409) {
                    completion.onCompletion(false, new OMHClientDataPointConflict());
                    return;
                }
                else if (responseCode == 502) {
                    completion.onCompletion(false, new OMHClientBadGateway());
                    return;
                }
                else {
                    String responseBody = "";
                    try {
                        responseBody = response.body().string();
                        JSONObject responseJson = new JSONObject(responseBody);

                        String error = responseJson.getString("error");
//                        String errorDescription = responseJson.getString("error_description");

                        if (error.equals("invalid_token")) {
                            completion.onCompletion(false, new OMHClientInvalidAccessToken());
                            return;
                        }
                        else {
                            completion.onCompletion(false, new OMHClientMalformedResponse(responseBody));
                            return;
                        }

                    } catch (JSONException e) {
//                        Log.e(TAG, "Fail to parse response from omh-sign-in endpoint:" + responseBody, e);
                        completion.onCompletion(false, new OMHClientMalformedResponse(responseBody));
                        return;
                    }
                }

            }
        };
    }

}
