package org.researchsuite.ohmageomhbackend;

import android.content.Context;

import org.researchsuite.omhclient.OMHDataPoint;
import org.researchsuite.rsrp.RSRPBackEnd;
import org.researchsuite.rsrp.RSRPIntermediateResult;

import org.researchsuite.ohmageomhbackend.ORBEIntermediateResultTransformer.ORBEIntermediateResultTransformerService;
import org.researchsuite.ohmageomhsdk.OhmageOMHManager;
/**
 * Created by jameskizer on 2/4/17.
 */
public class ORBEOhmageResultBackEnd implements RSRPBackEnd {

    private static ORBEOhmageResultBackEnd backEnd;
    public static synchronized ORBEOhmageResultBackEnd getInstance() {
        if (backEnd == null) {
            backEnd = new ORBEOhmageResultBackEnd();
        }
        return backEnd;
    }

    @Override
    public void add(Context context, RSRPIntermediateResult intermediateResult) {
        OMHDataPoint datapoint = ORBEIntermediateResultTransformerService.getInstance().transform(context, intermediateResult);

        if (datapoint != null) {
            OhmageOMHManager.getInstance().addDatapoint(datapoint, new OhmageOMHManager.Completion() {
                @Override
                public void onCompletion(Exception e) {

                    //

                }
            });
        }

    }
}
