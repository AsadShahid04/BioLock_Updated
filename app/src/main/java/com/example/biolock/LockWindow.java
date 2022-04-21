package com.example.biolock;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class LockWindow {
    // declaring required variables
    private Context context;
    private View mView;
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private LayoutInflater layoutInflater;
    private TextView mHeadingLabel;
    private ImageView mFingerprintImage;
    private TextView mParaLabel;
    private Cipher cipher;
    private KeyguardManager keyguardManager; //will be used to check whether security is enabled on lockscreen or not.
    private KeyStore keyStore;
    private String KEY_NAME = "AndroidKey";
    private FingerprintListener fingerprintListener;

    private FingerprintManager fingerprintManager;

    public LockWindow(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // set the layout parameters of the window
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window not visible
                    // through any transparent parts
                    PixelFormat.OPAQUE);
        }
        // getting a LayoutInflater
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.lock_window, null);
        // set onClickListener on the remove button, which removes
        // the view from the window
        // Define the position of the
        // window within the screen
        mParams.gravity = Gravity.CENTER;
        mWindowManager = (WindowManager)context.getSystemService(WINDOW_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void open() {
        try {
            // check if the view is already
            // inflated or present in the window
            if(mView.getWindowToken()==null) {
                if(mView.getParent()==null) {
                    mWindowManager.addView(mView, mParams);
                    fingerprintListener = new FingerprintListener();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
                        fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
                        mHeadingLabel = (TextView) mView.findViewById(R.id.headingLabel2);
                        mFingerprintImage = (ImageView) mView.findViewById(R.id.fingerprintImage2);
                        mParaLabel = (TextView) mView.findViewById(R.id.paraLabel2);
                        mParaLabel.setText("Place your Finger on the Scanner to Proceed!");
                        mFingerprintImage.setImageResource(R.mipmap.app_logo);

                        fingerprintListener.setOnAuthenticationListener(new FingerprintManager.AuthenticationCallback() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onAuthenticationError(int errorCode, CharSequence errString) {
                                mParaLabel.setText("Authentication error\n" + "Error code" + errorCode + "\nError String" + errString);
                                if (errorCode == FingerprintManager.FINGERPRINT_ERROR_CANCELED){
                                    close();
                                    open();
                                }
                            }

                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                                mParaLabel.setText("Authentication help\n" + helpString);
                            }

                            @Override
                            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                                mParaLabel.setText("Authentication Successful");
                                mFingerprintImage.setImageResource(R.mipmap.action_done);
                                close();
                            }

                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onAuthenticationFailed() {
                                mParaLabel.setText("Authentication failed");
                            }
                        });

                        generateKey();
                        if (cipherInit()){
                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            fingerprintListener.startListening(cryptoObject, context);
                        }
//            }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("Error1",e.toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void close() {
        try {
            // remove the view from the window
            ((WindowManager) context.getSystemService(WINDOW_SERVICE)).removeView(mView);
            // invalidate the view
            mView.invalidate();
            // remove all views
            ((ViewGroup)mView.getParent()).removeAllViews();
            fingerprintListener.stopListening();

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (Exception e) {
            Log.d("Error2",e.toString());
        }
    }

    //methods for CryptoObject
    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {

            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }
        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
}
