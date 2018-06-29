package com.example.anon.passmanager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;

/**
 * Created by Anon on 2017-02-12.
 */

public class GenerateSettingsDialog extends AlertDialog.Builder {
    App mApp = App.getAppInstance();

    public GenerateSettingsDialog(final Context context, final String source, final String lastGeneratedPwd, final Dialog dialog) {
        super(context);
        LayoutInflater li = LayoutInflater.from(context);
        View myView = li.inflate(R.layout.generate_settings_layout, null);

        final CheckBox
                cbLowercase = (CheckBox) myView.findViewById(R.id.cb_lowercase),
                cbUppercase = (CheckBox) myView.findViewById(R.id.cb_uppercase),
                cbNumbers = (CheckBox) myView.findViewById(R.id.cb_numbers),
                cbSymbols = (CheckBox) myView.findViewById(R.id.cb_symbols);
        
        setView(myView);
        setCancelable(false);

        cbLowercase.setChecked( mApp.getPref(mApp.PS_LOWERCASE, true));
        cbUppercase.setChecked( mApp.getPref(mApp.PS_UPPERCASE, true));
        cbNumbers.setChecked( mApp.getPref(mApp.PS_NUMBERS, true));
        cbSymbols.setChecked( mApp.getPref(mApp.PS_SYMBOLS, false));

        setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!cbLowercase.isChecked() && !cbUppercase.isChecked() &&
                        !cbNumbers.isChecked() && !cbSymbols.isChecked()) {
                    Toast.makeText(context, R.string.include_type_error, Toast.LENGTH_SHORT).show();
                }
                updatePasswordSettings(
                        cbLowercase.isChecked(), cbUppercase.isChecked(),
                        cbNumbers.isChecked(), cbSymbols.isChecked()
                );
                new GeneratePasswordDialog(context, source, null, dialog).show();
            }
        });
        setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new GeneratePasswordDialog(context, source, lastGeneratedPwd, dialog).show();
            }
        });
        create().show();
    }

    public void updatePasswordSettings(boolean lowercase, boolean uppercase,
                                       boolean numbers, boolean symbols) {
        mApp.putPrefs(mApp.PS_LOWERCASE, lowercase);
        mApp.putPrefs(mApp.PS_UPPERCASE, uppercase);
        mApp.putPrefs(mApp.PS_NUMBERS, numbers);
        mApp.putPrefs(mApp.PS_SYMBOLS, symbols);
    }
}
