package edu.cornell.tech.foundry.ohmageomhsdkrs;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.Toast;

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;

import edu.cornell.tech.foundry.ohmageomhsdk.OhmageOMHManager;

/**
 * Created by jameskizer on 2/6/17.
 */

public class CTFOhmageLogInStepLayout extends CTFLogInStepLayout {

    public CTFOhmageLogInStepLayout(Context context) {
        super(context);
    }

    public CTFOhmageLogInStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CTFOhmageLogInStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initialize(Step step, StepResult result) {
        super.initialize(step, result);
        this.passwordField.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @Override
    protected void loginButtonAction(String identity, String password, ActionCompletion completion) {

        final Activity activity = (Activity)this.context;
        OhmageOMHManager.getInstance().signIn(identity, password, new OhmageOMHManager.Completion() {
            @Override
            public void onCompletion(Exception e) {
                if (e == null) {
                    setLoggedIn(true);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            completion.onCompletion(true);
                        }
                    });

                }
                else {
                    setLoggedIn(false);

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), "Username / Password are not valid", Toast.LENGTH_SHORT)
                                    .show();

                            completion.onCompletion(false);
                        }
                    });



                }
            }
        });

    }

    @Override
    protected void forgotPasswordButtonAction(String identity, ActionCompletion completion) {

        final Activity activity = (Activity)this.context;

        setLoggedIn(false);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                completion.onCompletion(true);
            }
        });

    }


}
