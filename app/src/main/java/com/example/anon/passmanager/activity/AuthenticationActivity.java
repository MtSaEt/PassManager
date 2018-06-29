package com.example.anon.passmanager.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.helper.FingerprintHandler;
import com.example.anon.passmanager.R;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class AuthenticationActivity extends AppCompatActivity {
    App mApp = App.getAppInstance();
    // layout
    private TextView tvEnterPasscode, tvFingerprint;
    private Button number_1, number_2, number_3, number_4, number_5, number_6,
            number_7, number_8, number_9, number_0;
    private ImageButton btn_back;
    private ImageView ivFingerprint;
    LinearLayout viewPIN;

    private ArrayList<String> currentPIN = new ArrayList<>();
    Map<String, ImageView> IViews = new HashMap<>();
    
    Intent intentLaunched;

    // Auth
    boolean isEnabled = false, isAble = false, isMainLaunch = true;

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;

    private CancellationSignal cancellationSignal;


    public int getPINLength() {
        return getDefinedPIN().length(); // Pin Length
    }

    public String getDefinedPIN() {
        return mApp.getPref(mApp.USER_PIN, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMainLaunch = getIntent().getAction() != null;
        intentLaunched = getIntent();
        if (isSetup()) {
            Intent intent = new Intent(this, SetupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent); // Switch off
            finish(); // these two (maybe)
            return;
        } else {
            setContentView(R.layout.activity_authentication);
            retrieveComponents();
            initiateFingerprint();
        }
    }

    private void initiateFingerprint() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isEnabled = prefs.getBoolean(mApp.FINGERPRINT_SWITCH, false);

        if (isEnabled) {
            cancellationSignal = new CancellationSignal();

            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            if (!keyguardManager.isKeyguardSecure()) {
                tvFingerprint.setText(R.string.fingerprint_security_not_enabled);
                ivFingerprint.setVisibility(View.GONE);
                return;
            }

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.USE_FINGERPRINT) !=
                    PackageManager.PERMISSION_GRANTED) {
                tvFingerprint.setText(R.string.fingerprint_permission_not_enabled);
                ivFingerprint.setVisibility(View.GONE);
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    // This happens when no fingerprints are registered.
                    tvFingerprint.setText(R.string.fingerprint_no_fingerprints_registered);
                    ivFingerprint.setVisibility(View.GONE);
                    return;
                }
            }

            generateKey();
            isAble = true;

            if (cipherInit()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    FingerprintHandler helper = new FingerprintHandler(this);
                    tvFingerprint.setVisibility(View.GONE);
                    helper.startAuth(cancellationSignal, fingerprintManager, cryptoObject, intentLaunched);
                }
            }
        } else {
            ivFingerprint.setVisibility(View.GONE);
            tvFingerprint.setVisibility(View.GONE);
        }
    }

    private boolean isSetup()
    {
        boolean setup = false;
        if (getDefinedPIN() == null) {
            setup = true;
        }
        return setup; // setup here lol
    }

    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException(
                    "Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new
                        KeyGenParameterSpec.Builder(mApp.FINGERPRINT_KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(
                                KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(mApp.FINGERPRINT_KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onPause() {
        super.onPause();
        if (isAble) {
            if (cipherInit()) {
                cryptoObject = new FingerprintManager.CryptoObject(cipher);
                FingerprintHandler helper = new FingerprintHandler(this);
                cancellationSignal.cancel();
                helper.startAuth(cancellationSignal, fingerprintManager, cryptoObject, intentLaunched);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        if (isAble) {
            if (cipherInit()) {
                cryptoObject = new FingerprintManager.CryptoObject(cipher);
                FingerprintHandler helper = new FingerprintHandler(this);
                if (!isEnabled) {
                    cancellationSignal.cancel();
                    ivFingerprint.setVisibility(View.GONE);
                    tvFingerprint.setVisibility(View.GONE);
                } else {
                    cancellationSignal = new CancellationSignal();
                }
                helper.startAuth(cancellationSignal, fingerprintManager, cryptoObject, intentLaunched);
            }
        }
    }

    public AlertDialog.Builder wrongPinDialog() {
        TextView pwdText = new TextView(this);
        pwdText.setText(R.string.incorrect_authentication_pin);
        pwdText.setTextSize(18);
        pwdText.setPadding(0, 50, 0, 0);
        pwdText.setGravity(Gravity.CENTER_HORIZONTAL);

        AlertDialog.Builder dialogAlert = mApp.buildBasicDialog(
                this,
                getString(R.string.incorrect_authentication_pin_title),
                null, pwdText, R.drawable.ic_clear_black_24dp);
        dialogAlert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        return dialogAlert;
    }

    public void retrieveComponents() {
        number_1 = (Button) findViewById(R.id.number_1);
        number_2 = (Button) findViewById(R.id.number_2);
        number_3 = (Button) findViewById(R.id.number_3);
        number_4 = (Button) findViewById(R.id.number_4);
        number_5 = (Button) findViewById(R.id.number_5);
        number_6 = (Button) findViewById(R.id.number_6);
        number_7 = (Button) findViewById(R.id.number_7);
        number_8 = (Button) findViewById(R.id.number_8);
        number_9 = (Button) findViewById(R.id.number_9);
        number_0 = (Button) findViewById(R.id.number_0);

        tvEnterPasscode = (TextView) findViewById(R.id.tv_pin);
        tvFingerprint = (TextView) findViewById(R.id.tv_fingerprint);

        btn_back = (ImageButton) findViewById(R.id.btn_back);
        ivFingerprint = (ImageView) findViewById(R.id.ivFingerprint);

        viewPIN = (LinearLayout) findViewById(R.id.linearPINLayout);
        generatePINLength(getPINLength());

        number_1.setOnClickListener(listenBtn);
        number_2.setOnClickListener(listenBtn);
        number_3.setOnClickListener(listenBtn);
        number_4.setOnClickListener(listenBtn);
        number_5.setOnClickListener(listenBtn);
        number_6.setOnClickListener(listenBtn);
        number_7.setOnClickListener(listenBtn);
        number_8.setOnClickListener(listenBtn);
        number_9.setOnClickListener(listenBtn);
        number_0.setOnClickListener(listenBtn);
        btn_back.setOnClickListener(listenBtn);
    }

    private View.OnClickListener listenBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.number_1:
                case R.id.number_2:
                case R.id.number_3:
                case R.id.number_4:
                case R.id.number_5:
                case R.id.number_6:
                case R.id.number_7:
                case R.id.number_8:
                case R.id.number_9:
                case R.id.number_0:
                    if (currentPIN.size() < getPINLength()) {
                        currentPIN.add(((Button) view).getText().toString());
                        modifyPINView();
                    }
                    break;
                case R.id.btn_back:
                    if (currentPIN.size() > 0) {
                        currentPIN.remove(currentPIN.size() - 1);
                        modifyPINView();
                    }
                    break;
            }
        }
    };

    public void generatePINLength(int n) {
        tvEnterPasscode.setText(getString(R.string.enter_current_pin));

        for (int i = 1; i < n + 1; i++) {
            ImageView pinIV = new ImageView(AuthenticationActivity.this);
            pinIV.setId(View.generateViewId());
            pinIV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, (float) (n/n)));
            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            pinIV.getLayoutParams().height = dimensionInDp;
            IViews.put("pin" + i, pinIV);
            viewPIN.addView(pinIV);
        }
    }

    public void modifyPINView() {
        for (int i = 1; i < getPINLength() + 1; i++) {
            if (i < currentPIN.size() + 1) {
                IViews.get("pin" + i).setBackgroundColor(ResourcesCompat.getColor(getResources(),
                        R.color.pinBarColor, null));
            } else {
                IViews.get("pin" + i).setBackgroundColor(Color.TRANSPARENT);
            }
        }
        if (currentPIN.size() >= getPINLength()) {
            isMatchingPin();
        }
    }

    @Override
    public Context getApplicationContext() { return this; }

    public void isMatchingPin() {
        String[] currentPINArray = currentPIN.toArray(new String[currentPIN.size()]);
        String currentPINString = TextUtils.join("", currentPINArray);
        currentPINString = currentPINString.trim();
        if (currentPINString.equals(getDefinedPIN())) {
            if (isMainLaunch) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            } else {
                setResult(RESULT_OK, intentLaunched);
                finish();
            }
        } else {
            clearForm();
            wrongPinDialog().show();
        }
    }

    public void clearForm() {
        currentPIN.clear();
        modifyPINView();
    }
}
