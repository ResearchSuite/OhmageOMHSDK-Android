package edu.cornell.tech.foundry.ohmageomhsdkrs;

import android.content.Context;
import android.util.AttributeSet;

import org.researchsuite.rsuiteextensionscore.RSRedirectStepDelegate;
import org.researchsuite.rsuiteextensionscore.RSRedirectStepLayout;

/**
 * Created by jameskizer on 9/1/17.
 */

public class OhmageOMHRedirectAuthStepLayout extends RSRedirectStepLayout {
    public OhmageOMHRedirectAuthStepLayout(Context context) {
        super(context);
    }

    public OhmageOMHRedirectAuthStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OhmageOMHRedirectAuthStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected RSRedirectStepDelegate getDelegate() {
        return OhmageOMHRedirectAuthStepDelegate.getInstance();
    }
}
