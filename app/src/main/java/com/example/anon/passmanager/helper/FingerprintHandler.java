package com.example.anon.passmanager.helper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;
import com.example.anon.passmanager.R;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.activity.MainActivity;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Anon on 2016-12-16.
 */

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {    
    private CancellationSignal cancellationSignal;
    private Context appContext;
    private Intent mIntent;
    
    public FingerprintHandler(Context context) {
        appContext = context;
    }

    public void startAuth(CancellationSignal cancelSignal, FingerprintManager manager,
                          FingerprintManager.CryptoObject cryptoObject, Intent intent) {

        cancellationSignal = cancelSignal;
        mIntent = intent;
        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        if (!cancellationSignal.isCanceled()) {
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        /*Toast.makeText(appContext,
                "Authentication error\n" + errString,
                Toast.LENGTH_LONG).show();
                */
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        /*Toast.makeText(appContext,
                "Authentication help\n" + helpString,
                Toast.LENGTH_LONG).show();
                */
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(appContext, App.getAppInstance().getRes().getString(R.string.fingerprint_authentication_failed),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {
        if (mIntent.getAction() != null) {
            Intent intent = new Intent(appContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ((Activity) appContext).finish();
            appContext.startActivity(intent);
        } else {
            ((Activity) appContext).setResult(RESULT_OK, mIntent);
            ((Activity) appContext).finish();
        }
    }
}
