package com.example.anon.passmanager;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;

import com.example.anon.passmanager.adapter.SimplePasswordRecyclerAdapter;
import com.example.anon.passmanager.helper.DatabaseController;
import com.example.anon.passmanager.model.ItemType;
import com.example.anon.passmanager.model.SimplePassword;

import java.util.ArrayList;

/**
 * Created by Anon on 2017-02-05.
 */

public class App extends Application {
    public static App instance;
    public static int DEVICE_HEIGHT;
    
    // Public final strings
    public final String SORT_BY_NAME = "S_B_NAME";
    public final String SORT_BY_TYPE = "S_B_TYPE";
    public final String SORT_BY_DATE = "S_B_DATE";
    public final String SIMPLE_ADD_SOURCE = "ADD_SOURCE";
    public final String SIMPLE_EDIT_SOURCE = "EDIT_SOURCE";
    public final String EXPORT_FILE_NAME = "passmanager_export";
    public final int SORT_DEFAULT = 0; // 0 = Down / 1 = Up
    
    /* Global values */
    public boolean isViewPasswordActivityOpen = false; // Prevents double click on a card

    /* Preferences' Keys */
    public final String USER_PIN = "USER_PIN";
    public final String ENCRYPTION_KEY = "private_key";
    public final String FINGERPRINT_KEY_NAME = "example_key";
        /* Preference .xml */
        public final String MODIFY_PIN = "pin_modify";
        public final String FINGERPRINT_SWITCH = "fingerprint_switch";
        public final String DATABASE_IMPORT = "misc_import";
        public final String DATABASE_EXPORT = "misc_export";
        public final String USERNAME_COMPLETION = "username_completion";
        public final String ASK_USERNAME_COMPLETION = "ask_username_completion"; 
    
    public final String FIRST_ASK_PERMISSION = "FIRST_PERMISSION_ASK";
    
    public final String PS_LOWERCASE = "PASS_SETTINGS_LOWERCASE";
    public final String PS_UPPERCASE = "PASS_SETTINGS_UPPERCASE";
    public final String PS_NUMBERS = "PASS_SETTINGS_NUMBERS";
    public final String PS_SYMBOLS = "PASS_SETTINGS_SYMBOLS";
    public final String PS_SLENGTH = "PASS_SETTINGS_LENGTH";
        // Sort preferences
        public final String CURRENT_SIMPLE_SORT = "SIMPLE_SORT";
        public final String CURRENT_SIMPLE_SORT_DIRECTION = "SIMPLE_SORT_DIRECTION";

    // Types. adding here new types will add the type immediatly
    public final ItemType[] simple_type_items = {
            new ItemType("Website", R.drawable.ic_http_black_36dp),
            new ItemType("App", R.drawable.ic_screen_lock_portrait_black_36dp),
            new ItemType("Wi-Fi", R.drawable.ic_wifi_black_36dp, false)
    };
    
    public ArrayList<String> savedUsernames;

    private SharedPreferences prefs;
    private Resources res;
    
    private Context mContext;
    
    // SimplePassword
    private SimplePassword selectedSimplePwd;
    private int pwdPos;
    
    @Override
    public void onCreate() {
        super.onCreate();
        // Get preferences
        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        res = getResources();
        
        instance = this;

        DatabaseController.create(getApplicationContext());
        //DatabaseController.instance.clearUsernameTable();
        Point size = new Point();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        DEVICE_HEIGHT = size.y;
        
        loadUsernameAutoComplete();
    }

    public void loadUsernameAutoComplete() {
        savedUsernames = new ArrayList<>();
        Cursor res = DatabaseController.instance.getSavedUsernames();
        if (res.getCount() == 0) {
            return;
        }
        while (res.moveToNext()) {
            savedUsernames.add(res.getString(0));
        }
        res.close();
    }

    public static App getAppInstance() {
        return instance;
    }
    
    public int generatePwdSettingsCounter() {
        int generationTypeLength = 0;
        boolean[] types = {
                getPref(PS_LOWERCASE, true),
                getPref(PS_UPPERCASE, true),
                getPref(PS_NUMBERS, true),
                getPref(PS_SYMBOLS, false)
        };
        for (boolean type : types) {
            if (type)
                generationTypeLength++;
        }
        return generationTypeLength;
    }
    
    public void copyToClipboard(Context context, String str) {
        ClipboardManager clipboard = (ClipboardManager) (context).getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getRes().getString(R.string.app_name), str);
        clipboard.setPrimaryClip(clip);
    }
    
    public void clearClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) (context).getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getRes().getString(R.string.app_name), "");
        clipboard.setPrimaryClip(clip);
    }
    
    public AlertDialog.Builder buildBasicDialog(Context context, String title, String message,
                                           View view, int icon) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        if (view != null) {
            dialog.setView(view);
        }
        if (message != null) {
            dialog.setMessage(message);
        }
        dialog.setTitle(title);
        dialog.setIcon(icon);
        return dialog;
    }

    public void putPrefs(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public void putPrefs(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }
    
    public void putPrefs(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }
    
    public String getPref(String key, String def) {
        return prefs.getString(key, def);
    }

    public boolean getPref(String key, boolean def) {
        return prefs.getBoolean(key, def);
    }
    
    public int getPref(String key, int def) {
        return prefs.getInt(key, def);
    }
    
    public Resources getRes() {
        return res;
    }
    
    public SimplePassword getSelectedSimplePwd() {
        return selectedSimplePwd;
    }
    
    public void setSelectedSimplePwd(SimplePassword simplePassword) {
        selectedSimplePwd = simplePassword;
    }

    public int getPwdPos() {
        return pwdPos;
    }

    public void setPwdPos(int pwdPos) {
        this.pwdPos = pwdPos;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }
}
