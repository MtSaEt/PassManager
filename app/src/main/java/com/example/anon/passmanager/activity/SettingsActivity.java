package com.example.anon.passmanager.activity;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;
import com.example.anon.passmanager.dialog.ImportDialog;
import com.example.anon.passmanager.helper.DatabaseController;
import com.example.anon.passmanager.model.SimplePassword;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class SettingsActivity extends AppCompatActivity {
    final static int PIN_STATUS = 100, PIN_MODIFIED = 200;
    final private int REQUEST_CODE_ASK_PERMISSIONS_IMPORT = 123,
            REQUEST_CODE_ASK_PERMISSIONS_EXPORT = 124;

    Context mContext;
    Activity main_mActivity;
    String mFilename;
    Toolbar toolbar;
    String file_extension = ".csv";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.settings_toolbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MainPreferenceFragment()).commit();
    }


    private void showMessageOKCancel(String message, String positive,
                                     String negative, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SettingsActivity.this)
                .setMessage(message)
                .setPositiveButton(positive, okListener)
                .setNegativeButton(negative, null)
                .create()
                .show();
    }

    public void showExportConfirmation(final Activity ctx) {
        File root = Environment.getExternalStorageDirectory();

        LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.export_dialog_layout, null);

        TextView path = (TextView) view.findViewById(R.id.tv_export_path);
        final EditText filenameEditText = (EditText) view.findViewById(R.id.et_export_filename);
        final TextView filenameExt = (TextView) view.findViewById(R.id.export_filename_ext); 
        
        String appFileFolder = root.getAbsolutePath() + "/" + getString(R.string.app_name);
        
        path.setText(appFileFolder);
        filenameExt.setText(file_extension);
        
        int counter = 0;
        File dir = new File(appFileFolder);
        File file = new File(dir, App.instance.EXPORT_FILE_NAME + file_extension);
        String fileWithoutExt = App.instance.EXPORT_FILE_NAME;
        
        while (file.exists()) {
            counter++;
            file = new File(dir, App.instance.EXPORT_FILE_NAME + "(" + counter + ")" + file_extension);
            fileWithoutExt = App.instance.EXPORT_FILE_NAME + "(" + counter + ")";
        }
        mFilename = fileWithoutExt;
        filenameEditText.setText(fileWithoutExt);

        AlertDialog.Builder customDialog = App.instance.buildBasicDialog(ctx,
                getString(R.string.dialog_export), null, view, R.drawable.ic_file_upload_black_24dp);
        customDialog.setPositiveButton(R.string.dialog_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                main_mActivity = ctx;
                int hasWriteExternalStoragePermission = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    hasWriteExternalStoragePermission = ctx.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionHandler(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_ASK_PERMISSIONS_EXPORT);
                } else {
                    DatabaseController.instance.exportDB(ctx, filenameEditText.getText().toString());
                }
            }
        });
        customDialog.setNegativeButton(R.string.dialog_cancel, null);
        customDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS_EXPORT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    DatabaseController.instance.exportDB(main_mActivity, mFilename);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case REQUEST_CODE_ASK_PERMISSIONS_IMPORT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importPrep(main_mActivity);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private boolean requestPermissionHandler(final Activity activity, final String permission, final int requestCode) {
        boolean isFirstAsk = App.instance.getPref(App.instance.FIRST_ASK_PERMISSION, true);

        if (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    requestCode);
        } else {
            if (isFirstAsk) {
                App.instance.putPrefs(App.instance.FIRST_ASK_PERMISSION, false);
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        requestCode);
            } else {
                showMessageOKCancel(getString(R.string.required_permissions),
                        getString(R.string.dialog_permissions), getString(R.string.dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });
            }
        }

        return false;
    }

    public void importPrep(final Activity ctx) {
        main_mActivity = ctx;
        int hasWriteExternalStoragePermission = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasWriteExternalStoragePermission = ctx.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissionHandler(ctx, Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_ASK_PERMISSIONS_IMPORT);
        } else {
            final File mPath = new File(Environment.getExternalStorageDirectory() + "//" + getString(R.string.app_name)+ "//");
            ImportDialog fileDialog = new ImportDialog(this, mPath, ".csv");
            fileDialog.addFileListener(new ImportDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    DatabaseController.instance.showImportConfirmation(ctx, file);
                }
            });
            //fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
            //  public void directorySelected(File directory) {
            //      Log.d(getClass().getName(), "selected dir " + directory.toString());
            //  }
            //});
            //fileDialog.setSelectDirectoryOption(false);
            fileDialog.showDialog();
        }
    }

    public class ImportTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(SettingsActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setMessage(getString(R.string.dialog_loading));
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            DatabaseController.instance.deleteAllSimplePasswords();
            for (SimplePassword s : DatabaseController.instance.pwds) {
                DatabaseController.instance.insertSimplePassword(s);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            MainActivity.passObjArrayList.clear();
            for (SimplePassword s : DatabaseController.instance.pwds) {
                MainActivity.passObjArrayList.add(0, s);
                MainActivity.adapter.notifyItemInserted(0);
            }
            asyncDialog.dismiss();
            Toast.makeText(SettingsActivity.this, R.string.imported_successfully, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
        }

    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        App mApp = App.getAppInstance();
        Activity mActivity;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            Preference pinModify = findPreference(mApp.MODIFY_PIN);
            pinModify.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(mActivity.getApplicationContext(), AuthenticationActivity.class);
                    startActivityForResult(intent, PIN_STATUS);
                    return true;
                }
            });
            
            // Checking fingerprint capabilities
            final Preference fingerprint = findPreference(mApp.FINGERPRINT_SWITCH);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                fingerprint.setEnabled(false);
            } else {
                FingerprintManager fingerprintManager = (FingerprintManager)
                        mActivity.getSystemService(Context.FINGERPRINT_SERVICE);
                if (ActivityCompat.checkSelfPermission(mActivity,
                        Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    fingerprint.setEnabled(false);
                    return;
                }
                if (!fingerprintManager.isHardwareDetected()) {
                    fingerprint.setEnabled(false);
                }
            }

            final Preference importData = findPreference(mApp.DATABASE_IMPORT);
            importData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((SettingsActivity) mActivity).importPrep(mActivity);
                    return true;
                }
            });

            final Preference export = findPreference(mApp.DATABASE_EXPORT);
            export.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((SettingsActivity) mActivity).showExportConfirmation(mActivity);
                    return true;
                }
            });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // Remove dividers
            View rootView = getView();
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            list.setDivider(null);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PIN_STATUS) {
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(mActivity.getApplicationContext(), PinActivity.class);
                    startActivityForResult(intent, PIN_MODIFIED);
                }
            } else if (requestCode == PIN_MODIFIED) {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(mActivity.getApplicationContext(),
                            R.string.pin_updated, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            if (context instanceof Activity){
                mActivity =(Activity) context;
            }
        }
        
        // Support for Android 5.1
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mActivity = activity;
        }
    }
}