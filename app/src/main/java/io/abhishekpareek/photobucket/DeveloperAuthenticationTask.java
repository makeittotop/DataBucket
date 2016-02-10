package io.abhishekpareek.photobucket;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import io.abhishekpareek.devauth.client.Response;

/**
 * Created by apareek on 2/9/16.
 */
public class DeveloperAuthenticationTask extends
        AsyncTask<LoginCredentials, Void, Void> {

    // The user name or the developer user identifier you will pass to the
    // Amazon Cognito in the GetOpenIdTokenForDeveloperIdentity API
    private String userName;

    private boolean isSuccessful;

    private final Context context;

    public DeveloperAuthenticationTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(LoginCredentials... params) {

        Response response = DeveloperAuthenticationProvider
                .getDevAuthClientInstance()
                .login(params[0].getUsername(), params[0].getPassword());
        isSuccessful = response.requestWasSuccessful();
        userName = params[0].getUsername();

        if (isSuccessful) {
            CognitoSyncClientManager
                    .addLogins(
                            ((DeveloperAuthenticationProvider) CognitoSyncClientManager.credentialsProvider
                                    .getIdentityProvider()).getProviderName(),
                            userName);
            // Always remember to call refresh after updating the logins map
            ((DeveloperAuthenticationProvider) CognitoSyncClientManager.credentialsProvider
                    .getIdentityProvider()).refresh();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (!isSuccessful) {
            new AlertDialog.Builder(context).setTitle("Login error")
                    .setMessage("Username or password do not match!!").show();
        }
    }
}
