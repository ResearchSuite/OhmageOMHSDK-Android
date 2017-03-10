package edu.cornell.tech.foundry.ohmageomhsdkrs;

import org.researchstack.backbone.step.FormStep;
import org.researchstack.backbone.step.Step;

/**
 * Created by jameskizer on 2/6/17.
 */

public class CTFLogInStep extends Step {


    public String getLogInButtonTitle() {
        return logInButtonTitle;
    }

    public String getForgotPasswordButtonTitle() {
        return forgotPasswordButtonTitle;
    }

    private String logInButtonTitle = "Log In";

    public void setLogInButtonTitle(String logInButtonTitle) {
        this.logInButtonTitle = logInButtonTitle;
    }

    public void setForgotPasswordButtonTitle(String forgotPasswordButtonTitle) {
        this.forgotPasswordButtonTitle = forgotPasswordButtonTitle;
    }

    private String forgotPasswordButtonTitle;

    public CTFLogInStep(String identifier, String title, String text, Class logInLayoutClass) {
        super(identifier, title);
        this.setText(text);
        this.setStepLayoutClass(logInLayoutClass);
    }

    public CTFLogInStep(String identifier, String title, String text) {
        super(identifier, title);
        this.setText(text);
        this.setStepLayoutClass(CTFLogInStepLayout.class);
    }

}
