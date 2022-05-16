package com.example.biolock;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.widget.ImageView;
import android.widget.TextView;

//handles the fingerprint authentication within the app
@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    CancellationSignal cancellationSignal;

    public FingerprintHandler(Context context) {
        this.context = context;
    }

    //conducts an authentication of the fingerprint on the phone.
    public void startAutha(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, null, 0, this, null);
    }

    //on error, returns the error message to the user
    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        this.update("There was an Auth Error. " + errString, false);
    }

    //on failure, returns the failure message to the user
    @Override
    public void onAuthenticationFailed() {
        this.update("Auth Failed. ", false);
    }

    //on external error, returns the help message to the user
    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        this.update("Error: " + helpString, false);
    }

    //on success, moves the user to the app page
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("You can now access the app.", true);

        Intent letsgo = new Intent(context, com.example.biolock.MainActivity.class);
        context.startActivity(letsgo);
    }

    //updates objects on the screen to output the results of the processes ran above
    private void update(String s, boolean b) {
        cancellationSignal.cancel();
        TextView paraLabel = (TextView) ((Activity) context).findViewById(R.id.paraLabel);
        ImageView imageView = (ImageView) ((Activity) context).findViewById(R.id.fingerprintImage);

        paraLabel.setText(s);

        if (b == false) {

            //paraLabel.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

        } else {

            //paraLabel.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            imageView.setImageResource(R.mipmap.action_done);

        }

    }
}
