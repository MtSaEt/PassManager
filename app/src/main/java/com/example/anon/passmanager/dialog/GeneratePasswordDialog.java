package com.example.anon.passmanager.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;
import com.example.anon.passmanager.helper.PasswordGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Anon on 2017-02-12.
 */

public class GeneratePasswordDialog extends Dialog {
    private PasswordGenerator mPasswordGenerator;
    private App mApp = App.getAppInstance();
    private int generationTypeLength;
    private String lastGeneratedSimplePwd;
    private String generatedPassword;
    
    public GeneratePasswordDialog(final Context context, final String source, final String lasttvGeneratedPwd, final Dialog dialog) {
        super(context);
        
        mPasswordGenerator = new PasswordGenerator();
        
        setContentView(R.layout.generate_password_layout);
        setCancelable(false);
        
        lastGeneratedSimplePwd = lasttvGeneratedPwd;
        
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        getWindow().setAttributes(lp);

        final SeekBar sbtvPwdLength = (SeekBar) findViewById(R.id.sb_pwd_length);
        final TextView tvPwdLength = (TextView) findViewById(R.id.tv_pwd_length);
        final TextView tvGeneratedPwd = (TextView) findViewById(R.id.tv_generate_pwd);

        ImageView ivGenerate = (ImageView) findViewById(R.id.iv_generate);
        ImageView ivCopy = (ImageView) findViewById(R.id.iv_copy);
        ImageView ivSettings = (ImageView) findViewById(R.id.iv_settings);

        Button btnCancel = (Button) findViewById(R.id.btn_generate_cancel);
        Button btnApply = (Button) findViewById(R.id.btn_apply);

        final int length = mApp.getPref(mApp.PS_SLENGTH, 9);
        generationTypeLength = mApp.generatePwdSettingsCounter();
        // Default values
        sbtvPwdLength.setMax(25);
        if (length <= generationTypeLength) {
            sbtvPwdLength.setProgress(generationTypeLength);
            tvPwdLength.setText(String.format(mApp.getRes().getString(R.string.pwd_length),
                    generationTypeLength));
        } else {
            sbtvPwdLength.setProgress(length);
            tvPwdLength.setText(String.format(mApp.getRes().getString(R.string.pwd_length),
                    length));
        }

        if (lastGeneratedSimplePwd == null) {
            tvGeneratedPwd.setText(mPasswordGenerator.generatePassword(
                    mApp.getPref(mApp.PS_LOWERCASE, true),
                    mApp.getPref(mApp.PS_UPPERCASE, true),
                    mApp.getPref(mApp.PS_NUMBERS, true),
                    mApp.getPref(mApp.PS_SYMBOLS, false),
                    sbtvPwdLength.getProgress()
            ));
            lastGeneratedSimplePwd = tvGeneratedPwd.getText().toString();
        } else {
            tvGeneratedPwd.setText(lastGeneratedSimplePwd);
        }

        View.OnClickListener generateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_generate_cancel:
                        if (source.equals(mApp.SIMPLE_ADD_SOURCE)) {
                            ((AddDialog) dialog).updateGeneratedPwd(null);
                            dialog.show();
                        } else if (source.equals(mApp.SIMPLE_EDIT_SOURCE)) {
                            ((EditDialog) dialog).updateGeneratedPwd(null);
                            dialog.show();
                        }
                        dismiss();
                        lastGeneratedSimplePwd = null;
                        break;
                    case R.id.btn_apply:
                        generatedPassword = tvGeneratedPwd.getText().toString();
                        dismiss();
                        if (source.equals(mApp.SIMPLE_ADD_SOURCE)) {
                            ((AddDialog) dialog).updateGeneratedPwd(generatedPassword);
                            dialog.show();
                        } else if (source.equals(mApp.SIMPLE_EDIT_SOURCE)) {
                            ((EditDialog) dialog).updateGeneratedPwd(generatedPassword);
                            dialog.show();
                        }
                        break;
                    case R.id.iv_generate:
                        generationTypeLength = mApp.generatePwdSettingsCounter();
                        tvGeneratedPwd.setText(mPasswordGenerator.generatePassword(
                                mApp.getPref(mApp.PS_LOWERCASE, true),
                                mApp.getPref(mApp.PS_UPPERCASE, true),
                                mApp.getPref(mApp.PS_NUMBERS, true),
                                mApp.getPref(mApp.PS_SYMBOLS, false),
                                sbtvPwdLength.getProgress()
                        ));
                        lastGeneratedSimplePwd = tvGeneratedPwd.getText().toString();
                        break;
                    case R.id.iv_copy:
                        mApp.copyToClipboard(getContext(), tvGeneratedPwd.getText().toString());
                        Toast.makeText(getContext(), R.string.passsword_copied, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.iv_settings:
                        dismiss();
                        new GenerateSettingsDialog(context, source, lasttvGeneratedPwd, dialog);
                        break;
                }
            }
        };

        btnCancel.setOnClickListener(generateClickListener);
        btnApply.setOnClickListener(generateClickListener);
        ivCopy.setOnClickListener(generateClickListener);
        ivGenerate.setOnClickListener(generateClickListener);
        ivSettings.setOnClickListener(generateClickListener);

        sbtvPwdLength.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress_value;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress_value = progress;
                        if (progress < generationTypeLength) {
                            seekBar.setProgress(generationTypeLength);
                            progress_value = generationTypeLength;
                        }
                        tvPwdLength.setText(String.format(mApp.getRes().getString(R.string.pwd_length),
                                progress_value));
                        mApp.putPrefs(mApp.PS_SLENGTH, progress_value);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                }
        );

        show();
    }
}