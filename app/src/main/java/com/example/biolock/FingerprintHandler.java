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

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    CancellationSignal cancellationSignal;

    public FingerprintHandler(Context context) {
        this.context = context;
    }

    //Starts authentication of the fingerprint
    public void startAutha(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, null, 0, this, null);
    }

    //On error or failure, sends the error to the user
    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        this.update("There was an Auth Error. " + errString, false);
    }

    //On error or failure, sends the error to the user
    @Override
    public void onAuthenticationFailed() {
        this.update("Auth Failed. ", false);
    }

    //On error or failure, sends the error to the user
    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        this.update("Error: " + helpString, false);
    }

    //On success, sends the user into the next page
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("You can now access the app.", true);

        Intent letsgo = new Intent(context, com.example.biolock.MainActivity.class);
        context.startActivity(letsgo);
    }

    //On success, changing image to checkmark and changing text to reveal success
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
