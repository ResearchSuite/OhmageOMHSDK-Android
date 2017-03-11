package edu.cornell.tech.foundry.ohmageomhbackend.ORBEIntermediateResultTransformer;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import edu.cornell.tech.foundry.ohmageomhbackend.ORBEIntermediateResultTransformer.spi.ORBEIntermediateResultTransformer;
import edu.cornell.tech.foundry.omhclient.OMHDataPoint;
import edu.cornell.tech.foundry.researchsuiteresultprocessor.RSRPFrontEndServiceProvider.spi.RSRPFrontEnd;
import edu.cornell.tech.foundry.researchsuiteresultprocessor.RSRPIntermediateResult;

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
