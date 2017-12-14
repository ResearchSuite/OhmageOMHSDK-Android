package org.researchsuite.ohmageomhbackend.ORBEIntermediateResultTransformer;

import android.content.Context;
import android.support.annotation.Nullable;

import org.researchsuite.rsrp.RSRPIntermediateResult;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.researchsuite.ohmageomhbackend.ORBEIntermediateResultTransformer.spi.ORBEIntermediateResultTransformer;
import org.researchsuite.omhclient.OMHDataPoint;

/**
 * Created by jameskizer on 2/4/17.
 */
public class ORBEIntermediateResultTransformerService {

    private static ORBEIntermediateResultTransformerService service;
    private ServiceLoader<ORBEIntermediateResultTransformer> loader;

    private ORBEIntermediateResultTransformerService() {
        this.loader = ServiceLoader.load(ORBEIntermediateResultTransformer.class);
    }

    public static synchronized ORBEIntermediateResultTransformerService getInstance() {
        if (service == null) {
            service = new ORBEIntermediateResultTransformerService();
        }
        return service;
    }

    @Nullable
    public OMHDataPoint transform(Context context, RSRPIntermediateResult intermediateResult) {

        try {
            Iterator<ORBEIntermediateResultTransformer> transformers = this.loader.iterator();

            while (transformers.hasNext()) {
                ORBEIntermediateResultTransformer transformer = transformers.next();
                if (transformer.canTransform(intermediateResult)) {
                    OMHDataPoint datapoint = transformer.transform(context, intermediateResult);
                    if (datapoint != null) {
                        return datapoint;
                    }
                }
            }
        } catch (ServiceConfigurationError serviceError) {
            serviceError.printStackTrace();
            return null;
        }

        return null;

    }

}
