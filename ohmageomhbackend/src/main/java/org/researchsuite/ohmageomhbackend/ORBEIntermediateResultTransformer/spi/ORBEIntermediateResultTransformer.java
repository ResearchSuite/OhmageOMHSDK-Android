package org.researchsuite.ohmageomhbackend.ORBEIntermediateResultTransformer.spi;

import android.content.Context;

import org.researchsuite.rsrp.RSRPIntermediateResult;

import org.researchsuite.omhclient.OMHDataPoint;

/**
 * Created by jameskizer on 2/4/17.
 */
public interface ORBEIntermediateResultTransformer {

    OMHDataPoint transform(Context context, RSRPIntermediateResult intermediateResult);
    boolean canTransform(RSRPIntermediateResult intermediateResult);

}

