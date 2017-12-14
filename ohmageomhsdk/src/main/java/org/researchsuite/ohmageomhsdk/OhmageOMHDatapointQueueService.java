package org.researchsuite.ohmageomhsdk;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by jameskizer on 4/26/17.
 */

public class OhmageOMHDatapointQueueService extends IntentService {

    //stores ids
    ObjectQueue<String> ohmageOMHDatapointQueue;
    boolean isUploading = false;

    public OhmageOMHDatapointQueueService() {
        super("OhmageOMHDatapointQueueWorker");
    }

    @Override
    public void onCreate() {

        //load queue from disk
        String filename = "filename";
        File queueFile = new File(this.getFilesDir() + filename);
        OhmageOMHDatapointConverter converter = new OhmageOMHDatapointConverter();
        try {
            this.ohmageOMHDatapointQueue = new FileObjectQueue<>(queueFile, converter);
        } catch(IOException e) {

        }


    }

    @Override
    public void onDestroy() {

        //save queue to disk

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
