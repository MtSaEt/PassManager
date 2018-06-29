package com.example.anon.passmanager.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PinActivity extends AppCompatActivity {
    App mApp = App.getAppInstance();
    
    Spinner spinnerPINLength;
    TextView tvEnterPasscode;
    Button number_1, number_2, number_3, number_4, number_5, number_6,
            number_7, number_8, number_9, number_0;
    ImageButton btn_back;
    LinearLayout viewPIN;
    Intent intent;
    
    boolean isFirstPIN = true;

    ArrayList<String> firstPIN = new ArrayList<>();
    ArrayList<String> confirmationPIN = new ArrayList<>();
    Map<String, ImageView> IViews = new HashMap<>();

    int chosenPINLength = 4; // Default
    String old_length = String.valueOf(chosenPINLength);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        setContentView(R.layout.activity_pin);
        initializeComponents();
    }

    public void initializeComponents() {
        spinnerPINLength = (Spinner) findViewById(R.id.spinner_pin_length);
        spinnerPINLength.setOnItemSelectedListener(pinHandler);
        tvEnterPasscode = (TextView) findViewById(R.id.tv_pin);
                
        viewPIN = (LinearLayout) findViewById(R.id.linearPINLayout);

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
        btn_back = (ImageButton) findViewById(R.id.btn_back);

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
                    if (isFirstPIN) {
                        if (firstPIN.size() < chosenPINLength) {
                            firstPIN.add(((Button) view).getText().toString());
                            modifyPINView();
                        }
                    } else {
                        if (confirmationPIN.size() < chosenPINLength) {
                            confirmationPIN.add(((Button) view).getText().toString());
                            modifyPINView();
                        }
                    }
                    break;
                case R.id.btn_back:
                    if (isFirstPIN) {
                        if (firstPIN.size() > 0) {
                            firstPIN.remove(firstPIN.size() - 1);
                            modifyPINView();
                        }
                    } else {
                        if (confirmationPIN.size() > 0) {
                            confirmationPIN.remove(confirmationPIN.size() - 1);
                            modifyPINView();
                        }
                    }
                    break;
            }
        }
    };

    public void modifyPINView() {
        if (isFirstPIN) {
            for (int i = 1; i < chosenPINLength + 1; i++) {
                if (i < firstPIN.size() + 1) {
                    IViews.get("pin" + i).setBackgroundColor(ResourcesCompat.getColor(getResources(),
                            R.color.pinBarColor, null));
                } else {
                    IViews.get("pin" + i).setBackgroundColor(Color.TRANSPARENT);
                }
            }
            
            if (firstPIN.size() >= chosenPINLength) {
                recordSecondPIN();
            }
        } else {
            for (int i = 1; i < chosenPINLength + 1; i++) {
                if (i < confirmationPIN.size() + 1) {
                    IViews.get("pin" + i).setBackgroundColor(ResourcesCompat.getColor(getResources(),
                            R.color.pinBarColor, null));
                } else {
                    IViews.get("pin" + i).setBackgroundColor(Color.TRANSPARENT);
                }
            }

            if (confirmationPIN.size() >= chosenPINLength) {
                isMatchingPIN();
            }
        }
    }

    private AdapterView.OnItemSelectedListener pinHandler = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedView, int pos, long id) {
            clearForm();
            int pinLength = Integer.parseInt(parentView.getItemAtPosition(pos)
                    .toString().substring(0, 1));
            chosenPINLength = pinLength;
            isFirstPIN = true;
            generatePINLength(pinLength);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {}
    };

    public void generatePINLength(int n) {
        clearForm();
        
        tvEnterPasscode.setText((isFirstPIN) ?
                String.format(getString(R.string.enter_passcode), n) :
                getString(R.string.pin_confirmation));
        old_length = String.valueOf(n);

        for (int i = 1; i < n + 1; i++) {
            ImageView pinIV = new ImageView(PinActivity.this);
            pinIV.setId(View.generateViewId());
            pinIV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, (float) (n/n)));
            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            pinIV.getLayoutParams().height = dimensionInDp;
            IViews.put("pin" + i, pinIV);
            viewPIN.addView(pinIV);
        }
    }


    public void recordSecondPIN() {
        isFirstPIN = false;
        clearForm();
        generatePINLength(chosenPINLength); // Gets it back on track
    }

    public void isMatchingPIN() {
        if (firstPIN.equals(confirmationPIN)) {
            String finalPINArray[] = confirmationPIN.toArray(new String[confirmationPIN.size()]);
            String finalPIN = TextUtils.join("", finalPINArray);
            
            mApp.putPrefs(mApp.USER_PIN, finalPIN);
            if (getCallingActivity() != null) {
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
//                try {
//                    String salt = saltString(generateSalt());
//                    mApp.putPrefs(mApp.ENCRYPTION_KEY, salt);
//                } catch (GeneralSecurityException e) {
//                    e.printStackTrace();
//                }
                startActivity(i);
            }
        } else {
            isFirstPIN = true;
            generatePINLength(chosenPINLength); // Gets it back on track
            wrongPinDialog().show();
        }
    }

    public AlertDialog.Builder wrongPinDialog() {
        TextView pwdText = new TextView(this);
        pwdText.setText(R.string.wrong_pin_text);
        pwdText.setTextSize(18);
        pwdText.setPadding(0, 50, 0, 0);
        pwdText.setGravity(Gravity.CENTER_HORIZONTAL);

        AlertDialog.Builder dialogAlert = mApp.buildBasicDialog(
                this,
                getString(R.string.wrong_pin_title),
                null, pwdText, R.drawable.ic_clear_black_24dp);
        dialogAlert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        return dialogAlert;
    }

    public void clearForm() {
        viewPIN.removeAllViews();
        if (isFirstPIN) {
            firstPIN.clear();
        }
        IViews.clear();
        confirmationPIN.clear();
    }
}
