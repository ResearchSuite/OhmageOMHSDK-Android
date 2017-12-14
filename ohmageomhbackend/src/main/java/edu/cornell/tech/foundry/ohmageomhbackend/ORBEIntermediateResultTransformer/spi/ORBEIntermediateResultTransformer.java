package edu.cornell.tech.foundry.ohmageomhbackend.ORBEIntermediateResultTransformer.spi;

import android.content.Context;

import org.researchsuite.rsrp.RSRPIntermediateResult;

import edu.cornell.tech.foundry.omhclient.OMHDataPoint;

/**
 * Created by jameskizer on 2/4/17.
 */
public interface ORBEIntermediateResultTransformer {

    OMHDataPoint transform(Context context, RSRPIntermediateResult intermediateResult);
    boolean canTransform(RSRPIntermediateResult intermediateResult);

}

