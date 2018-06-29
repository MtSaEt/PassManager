package com.example.anon.passmanager.helper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;
import com.example.anon.passmanager.activity.SettingsActivity;
import com.example.anon.passmanager.model.SimplePassword;
import com.example.anon.passmanager.util.Formatter;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Anon on 2017-03-07.
 */

@SuppressWarnings("TryFinallyCanBeTryWithResources")
public class DatabaseController {
    App mApp = App.getAppInstance();

    public ArrayList<SimplePassword> pwds;
    /**
     * The singleton instance of DatabaseController class
     */
    public static DatabaseController instance = null;

    private SQLiteOpenHelper helper;

    private final String HEADER_TYPE = mApp.getRes().getString(R.string.header_type);
    private final String HEADER_ALIAS = mApp.getRes().getString(R.string.header_alias);
    private final String HEADER_USERNAME = mApp.getRes().getString(R.string.header_username);
    private final String HEADER_PASSWORD = mApp.getRes().getString(R.string.header_password);
    private final String HEADER_CREATED = mApp.getRes().getString(R.string.header_created);
    String file_extension = ".csv";

    private DatabaseController(Context context) {
        helper = new DatabaseHelper(context);
    }

    /**
     * Instantiates the singleton instance of DatabaseController class
     */
    public static void create(Context context) {
        instance = new DatabaseController(context);
    }

    // ADD
    public long insertSimplePassword(SimplePassword s) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseHelper.COL_ALIAS, s.getAlias());
        contentValues.put(DatabaseHelper.COL_NAME, s.getName());
        contentValues.put(DatabaseHelper.COL_PASSWORD, s.getPassword());
        contentValues.put(DatabaseHelper.COL_TYPE, s.getType());
        contentValues.put(DatabaseHelper.COL_DATE, s.getDate());
        contentValues.put(DatabaseHelper.COL_LAST_MODIFIED, s.getLastModified());

        return db.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
    }

    public long insertSimplePassword(String alias, String name, String pwd, int type, long date, long last_modified) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseHelper.COL_ALIAS, alias);
        contentValues.put(DatabaseHelper.COL_NAME, name);
        contentValues.put(DatabaseHelper.COL_PASSWORD, pwd);
        contentValues.put(DatabaseHelper.COL_TYPE, type);
        contentValues.put(DatabaseHelper.COL_DATE, date);
        contentValues.put(DatabaseHelper.COL_LAST_MODIFIED, last_modified);

        return db.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
    }

    // RETRIEVE
    public Cursor getAllSimplePasswords() {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME, null);
    }

    public ContentValues getFromId(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ContentValues row = new ContentValues();
        Cursor cur = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME + "WHERE " + DatabaseHelper.COL_ID + " = ?", new String[] { String.valueOf(id) });
        if (cur.moveToNext()) {
            row.put(DatabaseHelper.COL_ID, cur.getString(0));
            row.put(DatabaseHelper.COL_ALIAS, cur.getString(1));
            row.put(DatabaseHelper.COL_NAME, cur.getString(2));
            row.put(DatabaseHelper.COL_PASSWORD, cur.getString(3));
            row.put(DatabaseHelper.COL_TYPE, cur.getInt(4));
            row.put(DatabaseHelper.COL_DATE, cur.getString(5));
            row.put(DatabaseHelper.COL_LAST_MODIFIED, cur.getString(6));
        }
        cur.close();
        return row;
    }

    // DELETE
    public void deleteAllSimplePasswords() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(DatabaseHelper.TABLE_NAME, null, null);
    }

    public Integer deleteSimplePassword(SimplePassword s) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COL_ID + " = ?", new String[] {s.getId()});
    }

    public Integer deleteSimplePassword(String id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COL_ID + " = ?", new String[] {id});
    }

    // UPDATE
    public boolean updateSimplePassword(String id, String alias, String name, String password, int type, long date, long last_modified) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseHelper.COL_ID, id);
        contentValues.put(DatabaseHelper.COL_ALIAS, alias);
        contentValues.put(DatabaseHelper.COL_NAME, name);
        contentValues.put(DatabaseHelper.COL_PASSWORD, password);
        contentValues.put(DatabaseHelper.COL_TYPE, type);
        contentValues.put(DatabaseHelper.COL_DATE, date);
        contentValues.put(DatabaseHelper.COL_LAST_MODIFIED, last_modified);

        db.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.COL_ID + " = ?", new String[] { id });
        return true;
    }
    
    /* Undo */

    public void undoDeletion() {
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            Cursor c = db.query(
                    DatabaseHelper.TABLE_UNDO,
                    null, null, null, null, null, null
            );

            if (c != null) {
                while (c.moveToNext()) {
                    String query = c.getString(c.getColumnIndex(DatabaseHelper.COL_SQL));
                    if (query != null) {
                        Cursor nc = db.rawQuery(
                                query,
                                null
                        );

                        if (nc != null) {
                            nc.moveToFirst();
                            nc.close();
                        }
                    }
                }
                c.close();
            }
            clearUndoTable();
        } finally {
            db.close();
        }
    }

    public void clearUndoTable() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_UNDO);
    }
    
    /* Username learning */
    public Cursor  getSavedUsernames() {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERNAME, null);
    }

    public long insertUsername(String username) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        
        contentValues.put(DatabaseHelper.COL_USERNAME, username);
        long ay = db.insert(DatabaseHelper.TABLE_USERNAME, null, contentValues);
        return ay;
    }

    public Integer deleteUsername(String username) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_USERNAME, DatabaseHelper.COL_USERNAME + " = ?", new String[] { username });
    }

    public void clearUsernameTable() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_USERNAME);
    }

    // FUNCTIONALITIES
    public void showImportConfirmation(final Activity ctx, final File file) {
        // Import
        AlertDialog.Builder customDialog = App.instance.buildBasicDialog(ctx,
                mApp.getRes().getString(R.string.import_database_title),
                mApp.getRes().getString(R.string.import_database_text),
                null, R.drawable.ic_file_download_black_24dp);
        customDialog.setPositiveButton(R.string.dialog_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    verifyFile(ctx, file);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        customDialog.setNegativeButton(R.string.dialog_cancel, null);
        customDialog.show();

    }

    public void exportDB(Activity ctx, String filename) {
        String columnString =
                "\"" + HEADER_TYPE + "\"" + ","
                        + "\"" + HEADER_ALIAS + "\"" + ","
                        + "\"" + HEADER_USERNAME + "\"" + ","
                        + "\"" + HEADER_PASSWORD + "\"" + ","
                        + "\"" + HEADER_CREATED + "\"";
        String dataString = "";

        Cursor res = DatabaseController.instance.getAllSimplePasswords();
        if (res.getCount() == 0) {
            TextView pwdText = new TextView(ctx);
            pwdText.setText(R.string.empty_database_text);
            pwdText.setTextSize(20);
            pwdText.setPadding(0, 50, 0, 0);
            pwdText.setGravity(Gravity.CENTER_HORIZONTAL);

            AlertDialog.Builder dialogAlert = App.instance.buildBasicDialog(ctx,
                    mApp.getRes().getString(R.string.empty_database_title),
                    null, pwdText, R.drawable.ic_chrome_reader_mode_black_24dp);
            dialogAlert.setPositiveButton(R.string.dialog_ok, null);
            dialogAlert.show();
            return;
        }
        while (res.moveToNext()) {
            dataString += "\""
                    + App.instance.simple_type_items[Integer.parseInt(res.getString(4)) - 1].getName()
                    + "\",\"" + res.getString(1)
                    + "\",\"" + res.getString(2)
                    + "\",\"" + res.getString(3)
                    + "\",\"" + Formatter.formatFullDate(Long.valueOf(res.getString(5)))
                    + "\"\n";
        }
        res.close();

        String combinedString = columnString + "\n" + dataString;
    
        File file;
        File root = Environment.getExternalStorageDirectory();
        if (root.canWrite()) {
            String appFileFolder = root.getAbsolutePath() + "/" + mApp.getRes().getString(R.string.app_name);
            File dir = new File(appFileFolder);
            dir.mkdirs();

            int counter = 0;
            file = new File(dir, filename + file_extension);
            while (file.exists()) {
                counter++;
                file = new File(dir, filename + "(" + counter + ")" + file_extension);
            }

            AlertDialog.Builder loadingDialog = new AlertDialog.Builder(ctx);
            LayoutInflater li = LayoutInflater.from(ctx);
            View myView = li.inflate(R.layout.export_dialog_success_layout, null);

            TextView path = (TextView) myView.findViewById(R.id.tv_export_file);
            path.setText(file.getName());

            loadingDialog.setView(myView);
            loadingDialog.setTitle(R.string.export_success_title);
            loadingDialog.setIcon(R.drawable.ic_done_black_24dp);
            loadingDialog.setPositiveButton(R.string.dialog_ok, null);
            loadingDialog.show();

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.write(combinedString.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void verifyFile(Context ctx, File filename) throws IOException, ParseException {
        boolean relevantFile = true;
        CSVReader reader = new CSVReader(new FileReader(filename.getAbsolutePath()), ',');
        if (reader.readNext() == null) {
            Toast.makeText(ctx, "Bad file content. Importation is cancelled.", Toast.LENGTH_LONG).show();
            return;
        }
        reader = new CSVReader(new FileReader(filename.getAbsolutePath()), ',');
        pwds = new ArrayList<>();
        HashMap<String, Integer> typeHM = new HashMap<>();
        for (int i = 0; i < App.instance.simple_type_items.length; i++) {
            typeHM.put(App.instance.simple_type_items[i].getName(), i + 1);
        }

        // read line by line
        String[] record;
        int count = 0;
        while ((record = reader.readNext()) != null) {
            if (count == 0) {
                if (record.length != 5) {
                    relevantFile = false;
                    Toast.makeText(ctx, "Bad file 1" , Toast.LENGTH_LONG).show();
                    break;
                }
                if (!record[0].equals(HEADER_TYPE) ||
                        !record[1].equals(HEADER_ALIAS) ||
                        !record[2].equals(HEADER_USERNAME) ||
                        !record[3].equals(HEADER_PASSWORD) ||
                        !record[4].equals(HEADER_CREATED)) {
                    relevantFile = false;
                    Toast.makeText(ctx, "Bad file 2", Toast.LENGTH_LONG).show();
                    break;
                }
            } else {
                // Check if pwd content is legit
                //Log.d("coe", record.toString());
            }
            count++;
        }

        if (relevantFile) {
            reader = new CSVReader(new FileReader(filename.getAbsolutePath()), ',');
            String[] pwd;
            reader.readNext(); // Remove header
            while ((pwd = reader.readNext()) != null) {
                SimplePassword simplePassword = new SimplePassword();
                simplePassword.setType(typeHM.get(pwd[0]));
                simplePassword.setAlias(pwd[1]);
                simplePassword.setName(pwd[2]);
                simplePassword.setPassword(pwd[3]);
                simplePassword.setDate(new SimpleDateFormat("h:mm a - d LLL, yyyy").parse(pwd[4]).getTime());
                simplePassword.setLastModified(new SimpleDateFormat("h:mm a - d LLL, yyyy").parse(pwd[4]).getTime());
                pwds.add(simplePassword);
            }
            reader.close();
            ((SettingsActivity) ctx).new ImportTask().execute();
        }
    }
}
