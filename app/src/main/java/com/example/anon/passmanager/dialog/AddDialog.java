package com.example.anon.passmanager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;
import com.example.anon.passmanager.activity.MainActivity;
import com.example.anon.passmanager.helper.DatabaseController;
import com.example.anon.passmanager.model.SimplePassword;
import com.example.anon.passmanager.adapter.DialogArrayAdapter;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Anon on 2017-02-11.
 */

public class AddDialog extends Dialog {
    private App mApp = App.getAppInstance();
    private int selectedType = 1;
    
    private String source = "";
    private AutoCompleteTextView accountName;
    private TextInputLayout typeInputLayout, usernameInputLayout;
    private EditText accountPassword, accountAlias;
    private boolean isShown = false, isAutocompletionEnabled, isAskAutocompletionEnabled;
    

    public AddDialog(final Context context) {
        super(context);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        isAutocompletionEnabled = prefs.getBoolean(mApp.USERNAME_COMPLETION, false);
        isAskAutocompletionEnabled = prefs.getBoolean(mApp.ASK_USERNAME_COMPLETION, true);
        
        this.setContentView(R.layout.simple_dialog_layout);
        final Map<String, ImageView> IVTypes = new HashMap<>();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);

        Button add = (Button) findViewById(R.id.btn_add);
        Button cancel = (Button) findViewById(R.id.btn_cancel);
        Button generatePwd = (Button) findViewById(R.id.gene_pwd);
        typeInputLayout = (TextInputLayout) findViewById(R.id.type_input_layout); 
        usernameInputLayout = (TextInputLayout) findViewById(R.id.username_input_layout);
        
        accountAlias = (EditText) findViewById(R.id.account_alias);
        accountName = (AutoCompleteTextView) findViewById(R.id.account_username);
        if (isAutocompletionEnabled) {
            // Initiate the list
            mApp.loadUsernameAutoComplete();
            //String[] usernames = mApp.savedUsernames.toArray(new String[mApp.savedUsernames.size()]);
            DialogArrayAdapter adapter = new DialogArrayAdapter(context,
                    R.layout.list_auto_complete_multiline, mApp.savedUsernames, accountName);
            accountName.setAdapter(adapter);


            accountName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        accountName.showDropDown();
                    }
                }
            });
        }
        
        accountPassword = (EditText) findViewById(R.id.account_password);
        
        final LinearLayout typeLayout = (LinearLayout) findViewById(R.id.linear_type_layout);

        View.OnClickListener typeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < mApp.simple_type_items.length; i++) {
                    if (view == IVTypes.get(mApp.simple_type_items[i].toString())) {
                        int drawable;
                        if (i == 0)
                            drawable = R.drawable.border_selected_first;
                        else if (i == mApp.simple_type_items.length - 1)
                            drawable = R.drawable.border_selected_last;
                        else
                            drawable = R.drawable.border_selected;
                        IVTypes.get(mApp.simple_type_items[i].toString())
                                .setBackground(ResourcesCompat.getDrawable(mApp.getRes(), drawable, null));
                        typeInputLayout.setHint(String.format(mApp.getRes().getString(R.string.type_name),
                                mApp.simple_type_items[i].getName()));
                        selectedType = i + 1;
                    } else {
                        IVTypes.get(mApp.simple_type_items[i].toString())
                                .setBackground(null);
                    }
                }
                if (!mApp.simple_type_items[selectedType - 1].hasUsername()) {
                    usernameInputLayout.setVisibility(View.GONE);
                } else usernameInputLayout.setVisibility(View.VISIBLE);
            }
        };

        // Generation type
        for (int i = 0; i < mApp.simple_type_items.length; i++) {
            ImageView typeIV = new ImageView(context);
            typeIV.setId(View.generateViewId());
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            int left = 2,
                    top = 2,
                    right = 2,
                    bottom = 2;
            float scale = mApp.getRes().getDisplayMetrics().density;
            int dpAsPixels = (int) (2 * scale + 0.5f);
            if (i == 0) {
                linearParams.setMargins(dpAsPixels, dpAsPixels, right, dpAsPixels);
                typeIV.setPadding(8, top, right, bottom);
                typeIV.setBackground(ResourcesCompat.getDrawable(mApp.getRes(), R.drawable.border_selected_first, null));
            } else if (i == mApp.simple_type_items.length - 1) {
                linearParams.setMargins(left, dpAsPixels, dpAsPixels, dpAsPixels);
                typeIV.setPadding(left, top, right, bottom);
            } else {
                linearParams.setMargins(left, dpAsPixels, right, dpAsPixels);
                typeIV.setPadding(left, top, 8, bottom);
            }
            typeIV.setLayoutParams(linearParams);

            if (!mApp.simple_type_items[selectedType - 1].hasUsername()) {
                usernameInputLayout.setVisibility(View.GONE);
            } else usernameInputLayout.setVisibility(View.VISIBLE);
            typeIV.setImageDrawable(ResourcesCompat.getDrawable(mApp.getRes(), mApp.simple_type_items[i].getDrawable(), null));                
            typeIV.setOnClickListener(typeClickListener);
            IVTypes.put(mApp.simple_type_items[i].toString(), typeIV);
            typeLayout.addView(typeIV);
        }

        typeInputLayout.setHint(String.format(mApp.getRes().getString(R.string.type_name),
                mApp.simple_type_items[selectedType - 1].getName()));
        
        CheckBox showPassword = (CheckBox) findViewById(R.id.show_password);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean able = true;
                if (mApp.simple_type_items[selectedType - 1].hasUsername()) {
                    if (accountName.getText().toString().equals("") || accountPassword.getText().toString().equals("")
                            || accountAlias.getText().toString().equals("")) {
                        Toast.makeText(context, R.string.required_fields, Toast.LENGTH_SHORT).show();
                        able = false;
                    }
                } else {
                    if (accountPassword.getText().toString().equals("")
                            || accountAlias.getText().toString().equals("")) {
                        Toast.makeText(context, R.string.required_fields, Toast.LENGTH_SHORT).show();
                        able = false;
                    }
                }
                if (able) {
                    int chosen = selectedType;

                    long isInserted = DatabaseController.instance.insertSimplePassword(
                            accountAlias.getText().toString(),
                            accountName.getText().toString(),
                            accountPassword.getText().toString(),
                            chosen,
                            System.currentTimeMillis(),
                            System.currentTimeMillis());

                    SimplePassword pwd = new SimplePassword(
                            String.valueOf(isInserted),
                            accountAlias.getText().toString(),
                            accountName.getText().toString(),
                            accountPassword.getText().toString(),
                            chosen,
                            System.currentTimeMillis(),
                            System.currentTimeMillis());

                    if (isInserted != -1) {
                        checkSavedUsername(context, accountName.getText().toString(), pwd);
                    } else
                        Toast.makeText(context, R.string.account_error, Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShown) {
                    accountPassword.setTransformationMethod(new PasswordTransformationMethod());
                    isShown = false;
                } else {
                    accountPassword.setTransformationMethod(null);
                    isShown = true;
                }
            }
        });

        generatePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
                source = mApp.SIMPLE_ADD_SOURCE;
                new GeneratePasswordDialog(context, source, null, AddDialog.this).show();
            }
        });
    }

    private void checkSavedUsername(Context context, final String username, SimplePassword pwd) {        
        if (mApp.simple_type_items[pwd.getType() - 1].hasUsername() && isAutocompletionEnabled) {
            if (!mApp.savedUsernames.contains(username)) {
                TextView pwdText = new TextView(context);
                pwdText.setText("");
                pwdText.setTextSize(16);
                pwdText.setPadding(50, 50, 50, 50);
                pwdText.setGravity(Gravity.CENTER_HORIZONTAL);

                AlertDialog.Builder dialog =
                        mApp.buildBasicDialog(
                                context,
                                "Ease of Use",
                                "Would you like to add this username to the autocomplete list?",
                                null,
                                0
                        );
                dialog.setPositiveButton(R.string.dialog_yes, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseController.instance.insertUsername(username);
                    }
                });
                dialog.setNegativeButton(R.string.dialog_no, null);
                if (isAskAutocompletionEnabled) {
                    dialog.create().show();
                } else {
                    DatabaseController.instance.insertUsername(username);
                }
            }
        }
        updateMainView(context, pwd);
    }
    
    private void updateMainView(Context context, SimplePassword pwd) {
        Toast.makeText(context, R.string.account_added, Toast.LENGTH_SHORT).show();
        ((MainActivity) context).passObjArrayList.add(0, pwd);
        ((MainActivity) context).recyclerView.smoothScrollToPosition(0);
        ((MainActivity) context).adapter.notifyItemInserted(0);
    }

    public void updateGeneratedPwd(String pwd) {
        if (pwd != null)
            accountPassword.setText(pwd);
    }
}