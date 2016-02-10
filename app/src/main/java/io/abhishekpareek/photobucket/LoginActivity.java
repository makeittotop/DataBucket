package io.abhishekpareek.photobucket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.amazon.identity.auth.device.shared.APIListener;

public class LoginActivity extends Activity {
    private String TAG = getClass().getSimpleName();

    private static AmazonAuthorizationManager mAuthManager;
    public static AmazonAuthorizationManager getAuthManager() {
        return mAuthManager;
    }

    private static final String[] APP_SCOPES = {
            "profile"
    };
    private Button loginWithAmazonButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CognitoSyncClientManager.init(this);

        try {
            mAuthManager = new AmazonAuthorizationManager(getApplicationContext(), Bundle.EMPTY);

        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Login with Amazon isn't configured correctly. "
                    + "Thus it's disabled in this demo." + "Reason: " + e.getMessage());
        }

        loginWithAmazonButton = (Button) findViewById(R.id.loginWithAmazonButton);
        loginWithAmazonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authorize();
            }
        });

    }

    void authorize() {
        mAuthManager.authorize(APP_SCOPES, Bundle.EMPTY, new AuthorizeListener());

        return;
    }

    public class AuthorizeListener implements AuthorizationListener {

        /* There was an error during the attempt to authorize the application. */
        @Override
        public void onError(AuthError ae) {
            Log.e(TAG, "AuthError during authorization", ae);
        }

        /* Authorization was cancelled before it could be completed. */
        @Override
        public void onCancel(Bundle cause) {
            Log.e(TAG, "User cancelled authorization");
        }

        @Override
        public void onSuccess(Bundle bundle) {
            Log.i(TAG, "Auth successful. Start to getToken");

            mAuthManager.getToken(APP_SCOPES, new AuthTokenListener());

            mAuthManager.getProfile(new APIListener() {
                @Override
                public void onSuccess(Bundle response) {
                    Bundle profileBundle = response
                            .getBundle(AuthzConstants.BUNDLE_KEY.PROFILE.val);
                    final String name = profileBundle
                            .getString(AuthzConstants.PROFILE_KEY.NAME.val);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Hello " + name,
                                    Toast.LENGTH_LONG).show();

                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }
                    });
                }

                @Override
                public void onError(AuthError ae) {
                    Log.e(TAG, "AuthError during getProfile", ae);
                }
            });
        }
    }

    private class AuthTokenListener implements APIListener {
        @Override
        public void onSuccess(Bundle response) {
            final String token = response
                    .getString(AuthzConstants.BUNDLE_KEY.TOKEN.val);
            Log.i(TAG, "amazon token: " + token);
            CognitoSyncClientManager.addLogins("www.amazon.com", token);
        }

        @Override
        public void onError(AuthError ae) {
            Log.e(TAG, "Failed to get token", ae);
        }
    }
}
