package com.example.biolock;

//we will be importing the packages needed for working with the fingerprint sensor on the phone
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
//This class will help us in writting the various methods we will be using to configure actions for the program to do with biometric data gathered by the user's finger
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    CancellationSignal cancellationSignal;

    public FingerprintHandler(Context context) {

        this.context = context;

    }
    //This method is used for conducting an authentication of the fingerprints on the phone. CryptoObject helps the system identify
    //whether there has been a new fingerprint added to the device since the last time the app was run.
    public void startAutha(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, null, 0, this, null);
    }

    @Override
    //method to show to user for determining whether there has been an error within the fingerprint handler package running the processes regarding the fingerprint
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        this.update("There was an Auth Error. " + errString, false);
    }

    @Override
    //method to show the user if the authentication failed or is not correct
    public void onAuthenticationFailed() {
        this.update("Auth Failed. ", false);
    }

    @Override
    //method regarding external error messages. Will display error help string to help the user fix the error
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        this.update("Error: " + helpString, false);
    }

    @Override
    //method to describe what happens when the fingerprint authentication successfully runs through and there has been a match with the correct fingerprint
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("You can now access the app.", true);

        Intent letsgo = new Intent(context, com.example.biolock.MainActivity.class);
        context.startActivity(letsgo);
    }
    //method used for updated objects on the screen to output the results of the processes ran above
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
