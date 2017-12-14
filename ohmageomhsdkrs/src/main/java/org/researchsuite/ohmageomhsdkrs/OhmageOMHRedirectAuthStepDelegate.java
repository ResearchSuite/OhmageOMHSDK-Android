package org.researchsuite.ohmageomhsdkrs;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.researchstack.backbone.utils.LogExt;
import org.researchsuite.rsuiteextensionscore.RSRedirectStepDelegate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.researchsuite.ohmageomhsdk.OhmageOMHManager;

/**
 * Created by jameskizer on 9/1/17.
 */

public class OhmageOMHRedirectAuthStepDelegate implements RSRedirectStepDelegate {

    private Pattern urlPattern;
    private Boolean completed;
    private Throwable error;
    private OhmageOMHManager manager;

    private Handler mHandler;

    private static OhmageOMHRedirectAuthStepDelegate delegate = null;
    private static Object delegateLock = new Object();


    @Nullable
    public static OhmageOMHRedirectAuthStepDelegate getInstance() {
        synchronized (delegateLock) {
            return delegate;
        }
    }

    public static void config(OhmageOMHManager manager, String urlScheme, String urlEndpoint) {
        synchronized (delegateLock) {
            if (delegate == null) {
                delegate = new OhmageOMHRedirectAuthStepDelegate(manager, urlScheme, urlEndpoint);
            }
        }
    }

    private OhmageOMHRedirectAuthStepDelegate(OhmageOMHManager manager, String urlScheme, String urlEndpoint) {

        this.manager = manager;

        StringBuilder patternStringBuilder = new StringBuilder("^");
        patternStringBuilder.append(urlScheme);
        patternStringBuilder.append("://");
        patternStringBuilder.append(urlEndpoint);
        patternStringBuilder.append(".*");

        Pattern urlPattern = Pattern.compile(patternStringBuilder.toString());
        this.urlPattern = urlPattern;

        //initialize state
        this.completed = null;
        this.error = null;
    }

    static public void safeOpenURL(Context context, Uri url) {
        try {
            LogExt.d(OhmageOMHRedirectAuthStepDelegate.class.getSimpleName(), "Beginning redirect");
            Intent myIntent = new Intent(Intent.ACTION_VIEW, url);
            context.startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No application can handle this request. Please install a web browser or check your URL.",  Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void beginRedirect(Context context) {
        Uri url = this.manager.getAuthURI();
        OhmageOMHRedirectAuthStepDelegate.safeOpenURL(context, url);
    }

    @Override
    public Boolean isCompleted() {
        return this.completed;
    }

    @Override
    public Throwable getError() {
        return this.error;
    }

    @Override
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    @Override
    public Boolean handleURL(Uri uri) {
        Matcher m = urlPattern.matcher(uri.toString());
        if (m.matches()) {
            String code = uri.getQueryParameter("code");
            if(code != null) {

                this.manager.signIn(code, new OhmageOMHManager.Completion() {
                    @Override
                    public void onCompletion(Exception e) {
                        String logText = new StringBuilder("SignedIn: ").append(true).toString();
                        LogExt.d(OhmageOMHRedirectAuthStepDelegate.class.getSimpleName(), logText);
                        OhmageOMHRedirectAuthStepDelegate.this.completed = true;
                        OhmageOMHRedirectAuthStepDelegate.this.error = e;
                        if (mHandler != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Message msg = new Message();
                                    mHandler.sendMessage(msg);
                                }
                            });
                        }
                    }
                });
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }
}
