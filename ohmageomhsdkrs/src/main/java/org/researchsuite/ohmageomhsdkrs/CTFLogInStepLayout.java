package org.researchsuite.ohmageomhsdkrs;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.ui.step.layout.StepLayout;
import org.researchstack.backbone.ui.views.SubmitBar;
import org.researchstack.backbone.utils.ObservableUtils;
import org.researchstack.backbone.utils.TextUtils;
import org.researchstack.skin.DataProvider;
import org.researchstack.skin.task.SignInTask;
import org.researchstack.skin.ui.adapter.TextWatcherAdapter;

/**
 * Created by jameskizer on 2/6/17.
 */


//based on SignInStepLayout in ResearchStack:Skin
public class CTFLogInStepLayout extends RelativeLayout implements StepLayout {

    public interface ActionCompletion {
        void onCompletion(boolean moveForward);
    }

    public static String LoggedInResultIdentifier = "IsLoggedInResult";


    private View               progress;
    protected AppCompatEditText identityField;
    protected AppCompatEditText  passwordField;
    private TextView forgotPassword;
    private CTFLogInStep         step;
    private StepResult<Boolean> result;
    private StepCallbacks      callbacks;

    protected Context context;

    protected void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    private boolean loggedIn = false;

    public CTFLogInStepLayout(Context context)
    {
        super(context);
        this.context = context;
    }

    public CTFLogInStepLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
    }

    public CTFLogInStepLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public void initialize(Step step, StepResult result) {

        this.step = (CTFLogInStep) step;
        this.result = result == null ? new StepResult<>(step) : result;

        View layout = LayoutInflater.from(getContext()).inflate(R.layout.ctf_layout_sign_in, this, true);

        //set title and text
        TextView titleView = (TextView) layout.findViewById(R.id.title);
        titleView.setText(this.step.getTitle());
        if(this.step.getText() != null) {
            TextView textView = (TextView) layout.findViewById(R.id.text);
            textView.setText(this.step.getText());
            textView.setVisibility(VISIBLE);
        }

        progress = layout.findViewById(R.id.progress);

        TextView identityLabel = (TextView) layout.findViewById(R.id.identityLabel);
        identityLabel.setText(this.step.getIdentityFieldName());

        identityField = (AppCompatEditText) layout.findViewById(R.id.username);
        identityField.addTextChangedListener(new TextWatcherAdapter()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(! TextUtils.isEmpty(identityField.getError()))
                {
                    identityField.setError(null);
                }
            }
        });
        identityField.setHint(this.step.getIdentityFieldName());

        passwordField = (AppCompatEditText) layout.findViewById(R.id.password);
        passwordField.addTextChangedListener(new TextWatcherAdapter()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(! TextUtils.isEmpty(passwordField.getError()))
                {
                    passwordField.setError(null);
                }
            }
        });
        passwordField.setOnEditorActionListener((v, actionId, event) -> {
            if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                    (actionId == EditorInfo.IME_ACTION_DONE))
            {
                return true;
            }
            return false;
        });

        passwordField.setHint(this.step.getPasswordFieldName());

        TextView passwordLabel = (TextView) layout.findViewById(R.id.passwordLabel);
        passwordLabel.setText(this.step.getPasswordFieldName());

        forgotPassword = (TextView) layout.findViewById(R.id.forgot_password);
        if (this.step.getForgotPasswordButtonTitle() != null && !this.step.getForgotPasswordButtonTitle().isEmpty()) {

            forgotPassword.setText(this.step.getForgotPasswordButtonTitle());
            RxView.clicks(forgotPassword).subscribe(v -> {

                loggedIn = false;
                forgotPasswordButtonAction(identityField.getText().toString(), new ActionCompletion() {
                    @Override
                    public void onCompletion(boolean moveForward) {
                        if (moveForward) {
                            callbacks.onSaveStep(StepCallbacks.ACTION_NEXT, step, getResult());
                        }
                    }
                });

//                if(! isEmailValid())
//                {
//                    Toast.makeText(getContext(), org.researchstack.skin.R.string.rss_error_invalid_email, Toast.LENGTH_SHORT)
//                            .show();
//                    return;
//                }
//
//                DataProvider.getInstance()
//                        .forgotPassword(getContext(), email.getText().toString())
//                        .compose(ObservableUtils.applyDefault())
//                        .subscribe(dataResponse -> {
//                            Toast.makeText(getContext(), dataResponse.getMessage(), Toast.LENGTH_SHORT)
//                                    .show();
//                        });
            });


        }
        else {
            forgotPassword.setVisibility(GONE);
        }

        SubmitBar submitBar = (SubmitBar) findViewById(R.id.submit_bar);
        submitBar.setPositiveAction(v -> signIn());
        submitBar.setPositiveTitle(this.step.getLogInButtonTitle());
        submitBar.getNegativeActionView().setVisibility(GONE);

    }

    private void signIn() {
        final String identity = this.identityField.getText().toString();
        final String password = this.passwordField.getText().toString();

        progress.animate().alpha(1).withStartAction(() -> {
            progress.setVisibility(View.VISIBLE);
            progress.setAlpha(0);
        }).withEndAction(() -> {
            loginButtonAction(identity, password, new ActionCompletion() {
                @Override
                public void onCompletion(boolean moveForward) {

                    progress.animate().alpha(0).withEndAction(() -> progress.setVisibility(View.GONE));

                    if (moveForward) {
                        callbacks.onSaveStep(StepCallbacks.ACTION_NEXT, step, getResult());
                    }
                }
            });
        });
    }

    public StepResult getResult() {
        result.setResultForIdentifier(CTFLogInStepLayout.LoggedInResultIdentifier, new Boolean(this.loggedIn));
        return result;
    }
    @Override
    public View getLayout() {
        return this;
    }

    @Override
    public boolean isBackEventConsumed() {
        callbacks.onSaveStep(StepCallbacks.ACTION_PREV, step, this.getResult());
        return false;
    }

    @Override
    public void setCallbacks(StepCallbacks callbacks)
    {
        this.callbacks = callbacks;
    }

    protected void loginButtonAction(String identity, String password, final ActionCompletion completion) {

        final Activity activity = (Activity)this.context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                completion.onCompletion(true);
            }
        });

    }

    protected void forgotPasswordButtonAction(String identity, final ActionCompletion completion) {

        final Activity activity = (Activity)this.context;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                completion.onCompletion(false);
            }
        });

    }
}
