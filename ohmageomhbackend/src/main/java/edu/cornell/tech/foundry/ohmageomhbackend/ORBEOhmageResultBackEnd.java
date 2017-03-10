package edu.cornell.tech.foundry.ohmageomhbackend;

import android.content.Context;

import edu.cornell.tech.foundry.ohmageomhbackend.ORBEIntermediateResultTransformer.ORBEIntermediateResultTransformerService;
import edu.cornell.tech.foundry.ohmageomhsdk.OhmageOMHManager;
import edu.cornell.tech.foundry.ohmclient.OMHDataPoint;
import edu.cornell.tech.foundry.researchsuiteresultprocessor.RSRPBackEnd;
import edu.cornell.tech.foundry.researchsuiteresultprocessor.RSRPIntermediateResult;

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
