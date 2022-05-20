package com.example.biolock;

import static android.content.Context.FINGERPRINT_SERVICE;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

//Listens for a fingerprint
@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintListener {

    private static FingerprintManager.AuthenticationCallback mAuthenticationCallback;
    private static CancellationSignal mCancellationSignal;
    private static Context mContext;
    private static FingerprintManager mFingerprintManager;
    private static View mView;
    private static TextView paraLabel;

    //sets up the listener
    public void setOnAuthenticationListener(FingerprintManager.AuthenticationCallback listener) {
        mAuthenticationCallback = listener;
    }

    //restarts the listener
    public void restartListening() {
        if (isFingerScannerAvailableAndSet()) {
            try {
                Log.d("restart", "1");
                mCancellationSignal = new CancellationSignal();
                mFingerprintManager.authenticate(null, mCancellationSignal, 0 /* flags */, mAuthenticationCallback, new Handler(Looper.getMainLooper()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //starts listening for fingerprints
    public void startListening(FingerprintManager.CryptoObject cryptoObject, Context context) {
        this.mContext = context;
        mFingerprintManager = (FingerprintManager) mContext.getSystemService(FINGERPRINT_SERVICE);
        if (isFingerScannerAvailableAndSet()) {
            try {
                Log.d("startlisten", "1");
                mCancellationSignal = new CancellationSignal();
                mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, mAuthenticationCallback, new Handler(Looper.getMainLooper()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //stops listening for fingerprints
    public void stopListening() {
        if (isFingerScannerAvailableAndSet()) {
            try {
                Log.d("cancel", "1");
                mCancellationSignal.cancel();
                mCancellationSignal = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Checks if the phone has an avaliable fingerprint scanner and enrolled fingerprints
    public boolean isFingerScannerAvailableAndSet() {
        if (mFingerprintManager == null){
            Log.d("rror", "1");
            return false;
        }
        if (!mFingerprintManager.isHardwareDetected()) {
            Log.d("rorr", "2");
            return false;
        }
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            Log.d("rror", "3");
            return false;
        }
        return true;
    }
}
