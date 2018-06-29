package com.example.anon.passmanager.activity;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;
import com.example.anon.passmanager.dialog.EditDialog;
import com.example.anon.passmanager.helper.DatabaseController;
import com.example.anon.passmanager.model.SimplePassword;
import com.example.anon.passmanager.util.Formatter;
import com.example.anon.passmanager.util.OnSingleClickListener;
import com.klinker.android.sliding.SlidingActivity;

import static com.example.anon.passmanager.activity.MainActivity.adapter;
import static com.example.anon.passmanager.activity.MainActivity.passObjArrayList;

/**
 * Created by Anon on 2017-01-04.
 */

public class ViewPasswordActivity extends SlidingActivity {
    App mApp = App.getAppInstance();

    boolean isSeeing = false;
    private ImageView pwdType, ivViewEdit, ivViewDelete;
    private TextView tvViewUsername, tvPassword, tvDate, tvMiscInformation;
    private me.grantland.widget.AutofitTextView tvName;
    private LinearLayout miscInformationLayout;

    private SimplePassword selectedPwd;
    private String hiddenPwdString;

    @Override
    public void init(Bundle savedInstanceState) {
        setHeaderContent(R.layout.view_password_header_layout);
        setPrimaryColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimaryDark)
        );
        setFab(
                getResources().getColor(R.color.colorAccent),
                R.drawable.ic_visibility_off_white_24dp,
                clickListener
        );
        setContent(R.layout.activity_view_password);
        initializeComponenents();
        updateView(selectedPwd);
        enableFullscreen();
    }

    @Override
    public void finish() {
        mApp.isViewPasswordActivityOpen = false;
        super.finish();
    }

    public void initializeComponenents() {
        selectedPwd = mApp.getSelectedSimplePwd();

        pwdType = (ImageView) findViewById(R.id.pwd_type);
        /*pwdType.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (selectedPwd.getType() == 3) { // Wi-Fi
                   
                    
                }
                return false;
            }
        });
        */
        miscInformationLayout = (LinearLayout) findViewById(R.id.misc_information_layout); 
        
        tvName = (me.grantland.widget.AutofitTextView) findViewById(R.id.tv_name);
        tvViewUsername = (TextView) findViewById(R.id.tv_view_username); 
        tvPassword = (TextView) findViewById(R.id.tv_password);
        tvMiscInformation = (TextView) findViewById(R.id.tv_misc_information); 
        
        tvName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mApp.copyToClipboard(getApplicationContext(), tvName.getText().toString());
                Toast.makeText(ViewPasswordActivity.this, R.string.account_name_copied, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        tvPassword.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mApp.copyToClipboard(getApplicationContext(), selectedPwd.getPassword());
                Toast.makeText(ViewPasswordActivity.this, R.string.passsword_copied, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        
        tvMiscInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isVisible = miscInformationLayout.getVisibility() == View.VISIBLE;
                Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_upward_black_24dp, null);
                Drawable downArrow = getResources().getDrawable(R.drawable.ic_arrow_downward_black_24dp, null);
                if (isVisible) {
                    tvMiscInformation.setCompoundDrawablesWithIntrinsicBounds(downArrow, null, downArrow, null);
                    miscInformationLayout.setVisibility(View.GONE);
                } else {
                    tvMiscInformation.setCompoundDrawablesWithIntrinsicBounds(upArrow, null, upArrow, null);
                    miscInformationLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        // Removes that little shit
        View photoTouchInterceptOverlay =
                findViewById(com.klinker.android.sliding.R.id.photo_touch_intercept_overlay);
        ((ViewGroup) photoTouchInterceptOverlay.getParent()).removeView(photoTouchInterceptOverlay);

        tvDate = (TextView) findViewById(R.id.tv_date);
        ivViewEdit = (ImageView) findViewById(R.id.iv_view_edit);
        ivViewDelete = (ImageView) findViewById(R.id.iv_view_delete);

        ivViewEdit.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                new EditDialog(ViewPasswordActivity.this).show();
            }
        });

        ivViewDelete = (ImageView) findViewById(R.id.iv_view_delete);
        ivViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();
            }
        });
    }

    public void updateView(SimplePassword pwd) {
        selectedPwd = pwd;
        setTitle(selectedPwd.getAlias());
        if (!mApp.simple_type_items[selectedPwd.getType() - 1].hasUsername()) {
            tvViewUsername.setVisibility(View.GONE);
            tvName.setVisibility(View.GONE);
        } else {
            tvViewUsername.setVisibility(View.VISIBLE);
            tvName.setVisibility(View.VISIBLE);
        }
        hiddenPwdString = (generateHidden("*"));
        tvName.setText(selectedPwd.getName());
        Drawable drawable = ResourcesCompat.getDrawable(
                mApp.getRes(), mApp.simple_type_items[selectedPwd.getType() - 1].getDrawable(), null);
        pwdType.setImageDrawable(drawable);
        tvDate.setText(Formatter.formatFullDate(selectedPwd.getDate()));
        tvPassword.setText(generateHidden("*"));
    }

    public String generateHidden(String s) {
        String output = "";
        for (int i = 0; i < selectedPwd.getPassword().length(); i++) {
            output += s;
        }
        return output;
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fab:
                    if (!isSeeing) {
                        ((FloatingActionButton) view).setImageDrawable(
                                getResources().getDrawable(R.drawable.ic_visibility_white_24dp));
                        tvPassword.setText(selectedPwd.getPassword());
                        isSeeing = true;
                    } else {
                        ((FloatingActionButton) view).setImageDrawable(
                                getResources().getDrawable(R.drawable.ic_visibility_off_white_24dp));
                        tvPassword.setText(hiddenPwdString);
                        isSeeing = false;
                    }
                    break;
            }
        }
    };

    public void showDeleteDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        DatabaseController.instance.deleteSimplePassword(selectedPwd.getId());
                        passObjArrayList.remove(selectedPwd);
                        adapter.notifyItemRemoved(mApp.getPwdPos());
                        mApp.setSelectedSimplePwd(null);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ViewPasswordActivity.this);
        builder.setMessage(R.string.view_delete_confirmation).setPositiveButton(R.string.dialog_yes, dialogClickListener)
                .setNegativeButton(R.string.dialog_no, dialogClickListener).show();
    }
}
