package com.example.anon.passmanager.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;
import com.example.anon.passmanager.model.ItemType;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Anon on 2017-02-23.
 */

public class ImportDialog {
    private static final String PARENT_DIR = "\t..";
    private App mApp = App.getAppInstance();
    private ArrayList<ItemType> fileList = new ArrayList<>();
    private HashMap<String, Boolean> isDir = new HashMap<>();
    private File currentPath;

    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    public interface DirectorySelectedListener {
        void directorySelected(File directory);
    }

    private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<>();
    private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<>();
    private final Activity activity;
    private boolean selectDirectoryOption;
    private String fileEndsWith;

    /**
     * @param activity
     * @param initialPath
     */
    public ImportDialog(Activity activity, File initialPath) {
        this(activity, initialPath, null);
    }

    public ImportDialog(Activity activity, File initialPath, String fileEndsWith) {
        this.activity = activity;
        setFileEndsWith(fileEndsWith);
        if (!initialPath.exists()) initialPath = getExternalStorageDirectory();
        loadFileList(initialPath);
    }

    /**
     * @return file dialog
     */
    public Dialog createFileDialog() {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Custom title view for the dialog
        float scale = mApp.getRes().getDisplayMetrics().density;
        int dpAsPixels = (int) (16 * scale + 0.5f);
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(dpAsPixels, dpAsPixels, 0, 0);

        TextView title = new TextView(activity);
        TextView subtitle = new TextView(activity);

        title.setText(R.string.dialog_import);
        title.setTextSize(18);
        title.setTextColor(mApp.getRes().getColor(android.R.color.black));
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_file_download_black_24dp, 0, 0, 0);
        int dp5 = (int) (5 * mApp.getRes().getDisplayMetrics().density + 0.5f);
        title.setCompoundDrawablePadding(dp5);

        subtitle.setText(currentPath.getPath());
        subtitle.setTextSize(16);
        subtitle.setTypeface(null, Typeface.BOLD);
        linearLayout.addView(title);
        linearLayout.addView(subtitle);
        builder.setCustomTitle(linearLayout);
        builder.setNegativeButton(R.string.dialog_cancel, null);
        // End custom title view

        if (selectDirectoryOption) {
            builder.setPositiveButton(R.string.dialog_select_directory, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    fireDirectorySelectedEvent(currentPath);
                }
            });
        }

        ListAdapter adapter = new ArrayAdapter<ItemType>(
                activity,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                fileList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                tv.setText(fileList.get(position).getName());
                tv.setTextSize(16);
                tv.setCompoundDrawablesWithIntrinsicBounds(fileList.get(position).getDrawable(), 0, 0, 0);

                int dp5 = (int) (5 * mApp.getRes().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5);

                return view;
            }
        };

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String fileChosen = fileList.get(which).getName();
                File chosenFile = getChosenFile(fileChosen);
                if (chosenFile != null) {
                    if (chosenFile.canRead()) {
                        if (chosenFile.isDirectory()) {
                            loadFileList(chosenFile);
                            dialog.cancel();
                            dialog.dismiss();
                            showDialog();
                        } else fireFileSelectedEvent(chosenFile);
                    }  else fireFileSelectedEvent(null);
                } else fireFileSelectedEvent(chosenFile);
            }
        });

        dialog = builder.show();
        return dialog;
    }


    public void addFileListener(FileSelectedListener listener) {
        fileListenerList.add(listener);
    }

    public void removeFileListener(FileSelectedListener listener) {
        fileListenerList.remove(listener);
    }

    public void setSelectDirectoryOption(boolean selectDirectoryOption) {
        this.selectDirectoryOption = selectDirectoryOption;
    }

    public void addDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.add(listener);
    }

    public void removeDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.remove(listener);
    }

    /**
     * Show file dialog
     */
    public void showDialog() {
        createFileDialog().show();
    }

    private void fireFileSelectedEvent(final File file) {
        fileListenerList.fireEvent(new ListenerList.FireHandler<FileSelectedListener>() {
            public void fireEvent(FileSelectedListener listener) {
                if (file != null) {
                    listener.fileSelected(file);
                }
            }
        });
    }

    private void fireDirectorySelectedEvent(final File directory) {
        dirListenerList.fireEvent(new ListenerList.FireHandler<DirectorySelectedListener>() {
            public void fireEvent(DirectorySelectedListener listener) {
                listener.directorySelected(directory);
            }
        });
    }

    private void loadFileList(File path) {
        this.currentPath = path;
        fileList.clear();
        isDir.clear();
        if (path.exists()) {
            if (path.getParentFile() != null) {
                if (!isRootFolder(path)) {
                    fileList.add(new ItemType(PARENT_DIR, R.drawable.ic_folder_accent_24dp));
                }
            }
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    if (!sel.canRead()) return false;
                    if (selectDirectoryOption) {
                        isDir.put(sel.getName(), sel.isDirectory());
                        return sel.isDirectory();
                    }
                    else {
                        boolean endsWith = fileEndsWith != null ? filename.toLowerCase().endsWith(fileEndsWith) : true;
                        isDir.put(sel.getName(), sel.isDirectory());
                        return endsWith || sel.isDirectory();
                    }
                }
            };
            String[] filteredFileList = path.list(filter);
            for (String file : filteredFileList) {
                fileList.add(new ItemType(file, properFileListDrawable(isDir.get(file))));
            }
        }
    }

    private int properFileListDrawable(boolean isDirectory) {
        return (isDirectory) ? R.drawable.ic_folder_accent_24dp : R.drawable.ic_description_black_24dp;
    }

    private File getChosenFile(String fileChosen) {
        File file = null;
        if (!isRootFolder(currentPath) || !fileChosen.equals(PARENT_DIR)) {
            if (fileChosen.equals(PARENT_DIR))
                file = currentPath.getParentFile();
            else
                file = new File(currentPath, fileChosen);
        }
        return file;
    }

    private boolean isRootFolder(File currentPath) {
        boolean bool = false;
        if (currentPath.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            bool = true;
        }
        return bool;
    }

    private void setFileEndsWith(String fileEndsWith) {
        this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;
    }
}

class ListenerList<L> {
    private List<L> listenerList = new ArrayList<L>();

    public interface FireHandler<L> {
        void fireEvent(L listener);
    }

    public void add(L listener) {
        listenerList.add(listener);
    }

    public void fireEvent(FireHandler<L> fireHandler) {
        List<L> copy = new ArrayList<L>(listenerList);
        for (L l : copy) {
            fireHandler.fireEvent(l);
        }
    }

    public void remove(L listener) {
        listenerList.remove(listener);
    }

    public List<L> getListenerList() {
        return listenerList;
    }
}