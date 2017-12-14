package org.researchsuite.ohmageomhsdkrstb;

import com.google.gson.JsonObject;

import org.researchstack.backbone.step.Step;
import org.researchsuite.rsextensionsrstb.RSRedirectStepDescriptor;
import org.researchsuite.rstb.DefaultStepGenerators.RSTBBaseStepGenerator;
import org.researchsuite.rstb.RSTBTaskBuilderHelper;
import org.researchsuite.rsuiteextensionscore.RSRedirectStep;

import java.util.Arrays;

import org.researchsuite.ohmageomhsdkrs.OhmageOMHRedirectAuthStepLayout;

/**
 * Created by jameskizer on 9/1/17.
 */

public class OhmageOMHRedirectLoginStepGenerator extends RSTBBaseStepGenerator {

    public OhmageOMHRedirectLoginStepGenerator(){
        super();
        this.supportedTypes = Arrays.asList(
                "OhmageOMHRedirectLogin"
        );
    }

    @Override
    public Step generateStep(RSTBTaskBuilderHelper helper, String type, JsonObject jsonObject) {

        RSRedirectStepDescriptor stepDescriptor = helper.getGson().fromJson(jsonObject, RSRedirectStepDescriptor.class);

        RSRedirectStep step = new RSRedirectStep(
                stepDescriptor.identifier,
                OhmageOMHRedirectAuthStepLayout.class
        );

        step.setTitle(stepDescriptor.title);
        step.setText(stepDescriptor.text);
        step.setOptional(stepDescriptor.optional);
        step.setButtonText(stepDescriptor.buttonText);

        return step;

    }

}
