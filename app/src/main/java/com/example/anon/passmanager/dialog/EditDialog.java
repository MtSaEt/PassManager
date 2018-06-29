package com.example.anon.passmanager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
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
import com.example.anon.passmanager.activity.ViewPasswordActivity;
import com.example.anon.passmanager.helper.DatabaseController;
import com.example.anon.passmanager.model.SimplePassword;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Anon on 2017-02-12.
 */

public class EditDialog extends Dialog {
    private App mApp = App.getAppInstance();
    private int selectedType = 1;
    private String source = "";
    private AutoCompleteTextView accountName;
    private TextInputLayout typeInputLayout, usernameInputLayout;
    private EditText accountPassword, accountAlias;
    private boolean isShown = false;
    
    public EditDialog(final Context context) {
        super(context);
        this.setContentView(R.layout.simple_dialog_layout);
        final Map<String, ImageView> IVTypes = new HashMap<>();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.dimAmount = 0.95f; // Darker dim
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);

        Button save = (Button) findViewById(R.id.btn_add);
        Button cancel = (Button) findViewById(R.id.btn_cancel);
        Button generatePwd = (Button) findViewById(R.id.gene_pwd);
        typeInputLayout = (TextInputLayout) findViewById(R.id.type_input_layout);
        usernameInputLayout = (TextInputLayout) findViewById(R.id.username_input_layout);

        final SimplePassword selectedPwd = mApp.getSelectedSimplePwd();
        final int pwdPos = mApp.getPwdPos();
        
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
            int dpAsPixels = (int) (2*scale + 0.5f);
            if (i == 0) {
                linearParams.setMargins(dpAsPixels, dpAsPixels, right, dpAsPixels);
                typeIV.setPadding(8, top, right, bottom);
            } else if (i == mApp.simple_type_items.length - 1) {
                linearParams.setMargins(left, dpAsPixels, dpAsPixels, dpAsPixels);
                typeIV.setPadding(left, top, right, bottom);
            } else {
                linearParams.setMargins(left, dpAsPixels, right, dpAsPixels);
                typeIV.setPadding(left, top, 8, bottom);
            }
            typeIV.setLayoutParams(linearParams);
            if (selectedPwd.getType() != -1) { // lastType also
                if (i == selectedPwd.getType() - 1) {
                    if (selectedPwd.getType() - 1 == 0) {
                        typeIV.setBackground(ResourcesCompat.getDrawable(mApp.getRes(), R.drawable.border_selected_first, null));
                    } else if (selectedPwd.getType() - 1 == mApp.simple_type_items.length - 1) {
                        typeIV.setBackground(ResourcesCompat.getDrawable(mApp.getRes(), R.drawable.border_selected_last, null));
                    } else {
                        typeIV.setBackground(ResourcesCompat.getDrawable(mApp.getRes(), R.drawable.border_selected, null));
                    }
                    selectedType = i + 1;
                }
            }

            if (!mApp.simple_type_items[selectedType - 1].hasUsername()) {
                usernameInputLayout.setVisibility(View.GONE);
            } else usernameInputLayout.setVisibility(View.VISIBLE);
            //int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, mApp.getRes().getDisplayMetrics());
            typeIV.setImageDrawable(ResourcesCompat.getDrawable(mApp.getRes(), mApp.simple_type_items[i].getDrawable(), null));
            typeIV.setOnClickListener(typeClickListener);
            IVTypes.put(mApp.simple_type_items[i].toString(), typeIV);
            typeLayout.addView(typeIV);
        }        
       
        
        // *************** MAKE LASTPWD AND LASTNAME USEFUL ***************
        typeInputLayout.setHint(String.format(mApp.getRes().getString(R.string.type_name),
                mApp.simple_type_items[selectedType - 1].getName()));
        accountAlias = (EditText) findViewById(R.id.account_alias);
        
        accountName = (AutoCompleteTextView) findViewById(R.id.account_username);
        // Initiate the list
        String[] usernames = mApp.getRes().getStringArray(R.array.list_of_emails);
        ArrayAdapter<String> adapter = new ArrayAdapter
                (context, R.layout.list_auto_complete_multiline, usernames);
        accountName.setAdapter(adapter);
        
        accountPassword = (EditText) findViewById(R.id.account_password);
        //Restore data
        accountAlias.setText(selectedPwd.getAlias());
        accountName.setText(selectedPwd.getName());
        accountPassword.setText(selectedPwd.getPassword());
        CheckBox showPassword = (CheckBox) findViewById(R.id.show_password);
        save.setText(R.string.dialog_save);
        save.setOnClickListener(new View.OnClickListener() {
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
                    boolean isUpdated = DatabaseController.instance.updateSimplePassword(
                            selectedPwd.getId(),
                            accountAlias.getText().toString(),
                            accountName.getText().toString(),
                            accountPassword.getText().toString(),
                            chosen,
                            selectedPwd.getDate(),
                            System.currentTimeMillis());
                    if (isUpdated) {
                        Toast.makeText(context, R.string.account_updated, Toast.LENGTH_LONG).show();
                        SimplePassword editedPwd = new SimplePassword(
                                selectedPwd.getId(),
                                accountAlias.getText().toString(),
                                accountName.getText().toString(),
                                accountPassword.getText().toString(),
                                chosen,
                                selectedPwd.getDate(),
                                System.currentTimeMillis());
                        MainActivity.passObjArrayList.set(pwdPos, editedPwd);
                        MainActivity.adapter.notifyItemChanged(pwdPos);
                        if (mApp.isViewPasswordActivityOpen) {
                            ((ViewPasswordActivity) context).updateView(editedPwd);
                        }
                        mApp.setSelectedSimplePwd(editedPwd);
                    } else
                        Toast.makeText(context, R.string.account_error, Toast.LENGTH_LONG).show();
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
                source = mApp.SIMPLE_EDIT_SOURCE;
                new GeneratePasswordDialog(context, source, null, EditDialog.this).show();
            }
        });
    }

    public void updateGeneratedPwd(String pwd) {
        if (pwd != null)
            accountPassword.setText(pwd);
    }
}