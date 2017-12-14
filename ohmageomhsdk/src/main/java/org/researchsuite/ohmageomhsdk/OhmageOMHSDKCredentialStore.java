package org.researchsuite.ohmageomhsdk;

import android.content.Context;

/**
 * Created by jameskizer on 2/6/17.
 */

public interface OhmageOMHSDKCredentialStore {
    byte[] get(Context context, String key);
    void set(Context context, String key, byte[] value);
    void remove(Context context, String key);
    boolean has(Context context, String key);

}
