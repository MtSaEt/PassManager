package com.example.anon.passmanager.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;
import com.example.anon.passmanager.adapter.SimplePasswordRecyclerAdapter;
import com.example.anon.passmanager.dialog.AddDialog;
import com.example.anon.passmanager.dialog.EditDialog;
import com.example.anon.passmanager.helper.DatabaseController;
import com.example.anon.passmanager.helper.WrapContentLinearLayoutManager;
import com.example.anon.passmanager.model.SimplePassword;
import com.example.anon.passmanager.util.CustomRecyclerScrollViewListener;
import com.example.anon.passmanager.util.OnSingleClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;

public class MainActivity extends AppCompatActivity implements SimplePasswordRecyclerAdapter.PasswordAdapterCallback {
    App mApp = App.getAppInstance();

    Toolbar toolbar;
    public Toolbar selectionToolbar;
    private TextView selectionCounter;
    public boolean selectionState = false, isFabHidden = false;

    public RecyclerView recyclerView;
    public static SimplePasswordRecyclerAdapter adapter;
    public static ArrayList<SimplePassword> passObjArrayList;

    private FloatingActionButton fabSimple;
    SwipeRefreshLayout swipeToRefresh;
    
    // Sort items
    private MenuItem mi_sortByName, mi_sortByType, mi_sortByDate, searchItem, sortItem;
    private Drawable upArrow = ResourcesCompat.getDrawable(mApp.getRes(), R.drawable.ic_arrow_upward_black_24dp, null).getCurrent();
    private Drawable downArrow = ResourcesCompat.getDrawable(mApp.getRes(), R.drawable.ic_arrow_downward_black_24dp, null).getCurrent();
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initializeComponents();

        fabSimple = (FloatingActionButton) findViewById(R.id.fab_simple);
        //fabAdvanced = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_advanced);

        fabSimple.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                new AddDialog(MainActivity.this).show();
            }
        });
    }

    public void initializeComponents() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));

        FadeInLeftAnimator animator = new FadeInLeftAnimator();
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        recyclerView.setItemAnimator(animator);

        recyclerView.getItemAnimator().setAddDuration(250);
        recyclerView.getItemAnimator().setRemoveDuration(225);
        recyclerView.getItemAnimator().setChangeDuration(300);

        initiateToolbarActions();

        populateArrayList();
        adapter = new SimplePasswordRecyclerAdapter(this, passObjArrayList);
        recyclerView.setAdapter(adapter);

        CustomRecyclerScrollViewListener customRecyclerScrollViewListener
                = new CustomRecyclerScrollViewListener() {
            @Override
            public void show() {
                isFabHidden = false;
                fabSimple.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }
            @Override
            public void hide() {
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)fabSimple.getLayoutParams();
                int fabMargin = lp.bottomMargin;
                isFabHidden = true;
                fabSimple.animate().translationY(fabSimple.getHeight()+fabMargin).setInterpolator(new AccelerateInterpolator(2.0f)).start();
            }
        };
        recyclerView.addOnScrollListener(customRecyclerScrollViewListener);

        String currentSort = mApp.getPref(mApp.CURRENT_SIMPLE_SORT, mApp.SORT_BY_DATE);  // initial sort; by date
        int direction = mApp.getPref(mApp.CURRENT_SIMPLE_SORT_DIRECTION, ((mApp.SORT_DEFAULT - 1) * -1)); // default: down after check
        adapter.sortBy(currentSort, direction);

        /*swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener( ){
            @Override
            public void onRefresh() {
                refreshRecyclerView();
            }
        });
        */
    }

    public void initiateToolbarActions() {
        selectionToolbar = (Toolbar) findViewById(R.id.selection_toolbar);
        selectionCounter = (TextView) selectionToolbar.findViewById(R.id.selection_counter);
        selectionToolbar.findViewById(R.id.selection_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.deselectAll(true);
                toggleSelection(false);
            }
        });

        selectionToolbar.findViewById(R.id.selection_edit).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                mApp.setSelectedSimplePwd(adapter.selectedList.get(0)); // There's only one selected
                mApp.setPwdPos(adapter.viewHolderList.get(0).getAdapterPosition());
                adapter.deselectAll(true);
                toggleSelection(false);
                new EditDialog(MainActivity.this).show();
            }
        });

        selectionToolbar.findViewById(R.id.selection_delete).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                final ArrayList<SimplePassword> undos = new ArrayList<>();
                undos.addAll(adapter.selectedList);
                if (isFabHidden) {
                    isFabHidden = false;
                    fabSimple.show();
                    fabSimple.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                }
                toggleSelection(false);

                new Thread() {
                    @Override
                    public void run() {
                        final int length = adapter.selectedList.size();
                        final int[] sortablePosition = new int[length];

                        for (int i = 0; i < length; i++) {
                            SimplePassword s = undos.get(i);
                            int position = passObjArrayList.indexOf(s);
                            s.position = position;
                            sortablePosition[i] = position;
                        }

                        DatabaseController.instance.clearUndoTable(); // Clears it 
                        // before feeding the new batch

                        Arrays.sort(sortablePosition);
                        for (SimplePassword s : undos) {
                            DatabaseController.instance.deleteSimplePassword(s); // delete from db
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = length - 1; i >= 0; i--) {
                                    passObjArrayList.remove(sortablePosition[i]);
                                    adapter.notifyItemRemoved(sortablePosition[i]);
                                }
                                adapter.deselectAll(false);

                                StringBuilder message = new StringBuilder();
                                message.append(length).append(" ").append(getString(R.string.snackbar_undo_item_name));
                                if (length > 1) message.append(getString(R.string.snackbar_undo_multiple_delete));
                                else message.append(" " + getString(R.string.snackbar_undo_single_delete));

                                final Snackbar sbUndo = Snackbar.make(fabSimple != null ? fabSimple : selectionToolbar, message.toString(), 7000);
                                sbUndo.setAction(R.string.snackbar_undo_undo, new OnSingleClickListener() {
                                    @Override
                                    public void onSingleClick(View view) {
                                        sbUndo.setAction(R.string.snackbar_undo_undo, null);
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                DatabaseController.instance.undoDeletion();
                                                // Undo the deletion

                                                Collections.sort(undos, new Comparator<SimplePassword>() {
                                                    @Override
                                                    public int compare(SimplePassword t1, SimplePassword t2) {
                                                        if (t1.position < t2.position) return -1;
                                                        if (t1.position == t2.position) return 0;
                                                        return 1;
                                                    }
                                                });

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        for (int i = 0; i < length; i++) {
                                                            SimplePassword s = undos.get(i);
                                                            s.isSelected = false;
                                                            passObjArrayList.add(s.position, s);
                                                            adapter.notifyItemInserted(s.position);
                                                        }
                                                    }
                                                });
                                                interrupt();
                                            }
                                        }.start();
                                    }
                                });
                                sbUndo.show();
                            }
                        });
                        interrupt();
                    }
                }.start();
            }
        });
    }

    public void onChangeCounter(int count) {
        selectionCounter.setText(String.format(Locale.US, "%d " + getString(R.string.toolbar_selection_counter), count));
    }

    public void toggleSelection(boolean state) {
        selectionState = state;
        if (state) {
            com.example.anon.passmanager.util.Animator.create(MainActivity.this)
                    .on(selectionToolbar)
                    .setStartVisibility(View.VISIBLE)
                    .animate(R.anim.fab_scroll_in);
            fabSimple.hide();
        } else {
            com.example.anon.passmanager.util.Animator.create(MainActivity.this)
                    .on(selectionToolbar)
                    .setEndVisibility(View.GONE)
                    .animate(R.anim.fab_scroll_out);
            fabSimple.show();
        }
    }

    public void createPopupMenu(View view, final int position) {
        /*mApp.setSelectedSimplePwd(passObjArrayList.get(position));
        mApp.setPwdPos(position);
        final CharSequence[] items = {"View", "Edit", "Delete", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Take action");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        Log.d("item", item + "");
                        break;
                    case 1:
                        Log.d("item", item + "");
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        });
        builder.create().show();
        */
    }

    @Override
    public void showPopup(View view, int position) {
        createPopupMenu(view, position);
    }

    @Override
    public void showViewPasswordActivitity(View view, int pos) {
        if (!mApp.isViewPasswordActivityOpen) {
            mApp.setSelectedSimplePwd(passObjArrayList.get(pos));
            mApp.setPwdPos(pos);
            Intent viewActivity = new Intent(MainActivity.this, ViewPasswordActivity.class);
            startActivity(viewActivity);
            mApp.isViewPasswordActivityOpen = true;
        }
    }

    /*public void refreshRecyclerView() {
        passObjArrayList.clear();
        populateArrayList();
        swipeToRefresh.setRefreshing(false);
    }
    */

    public void populateArrayList() {
        passObjArrayList = new ArrayList<>();
        Cursor res = DatabaseController.instance.getAllSimplePasswords();
        if (res.getCount() == 0) {
            return;
        }
        while (res.moveToNext()) {
            SimplePassword simplePassword = new SimplePassword(
                    res.getString(0),
                    res.getString(1),
                    res.getString(2),
                    res.getString(3),
                    res.getInt(4),
                    res.getLong(5),
                    res.getLong(6));
            passObjArrayList.add(0, simplePassword);
        }
        adapter = new SimplePasswordRecyclerAdapter(this, passObjArrayList);
        recyclerView.setAdapter(adapter);
        res.close();
    }

    public void updateSort(String sort) {
        MenuItem[] menuItems = { mi_sortByName, mi_sortByType, mi_sortByDate };
        String[] sorts = { mApp.SORT_BY_NAME, mApp.SORT_BY_TYPE, mApp.SORT_BY_DATE };
        String currentSort = mApp.getPref(mApp.CURRENT_SIMPLE_SORT, mApp.SORT_BY_DATE);
        int direction = mApp.getPref(mApp.CURRENT_SIMPLE_SORT_DIRECTION, ((mApp.SORT_DEFAULT - 1) * -1)); // default: down after check
        Drawable drawable;

        if (direction == 0) { // was down
            drawable = upArrow;
            direction = 1;
        } else { // was up
            if (currentSort.equals(sort)) { // resets the pointer if the sort changes
                drawable = downArrow;
                direction = 0;
            } else {
                drawable = upArrow;
                direction = 1;
            }
        }

        for (int i = 0; i < menuItems.length; i++) {
            if (sorts[i].equals(sort)) {
                menuItems[i].setIcon(drawable);
            } else {
                menuItems[i].setIcon(null);
            }
        }
        mApp.putPrefs(mApp.CURRENT_SIMPLE_SORT, sort);
        mApp.putPrefs(mApp.CURRENT_SIMPLE_SORT_DIRECTION, direction);
        adapter.sortBy(sort, direction);
    }

    @Override
    public void onBackPressed() {
        if (selectionState) {
            adapter.deselectAll(true);
            toggleSelection(false);
        } else super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        mi_sortByName = menu.findItem(R.id.sort_by_name);
        mi_sortByType = menu.findItem(R.id.sort_by_type);
        mi_sortByDate = menu.findItem(R.id.sort_by_date);

        searchItem = menu.findItem(R.id.action_search);
        sortItem = menu.findItem(R.id.sort_by);

        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        fabSimple.show();
                        adapter.restoreAdapterList();
                        sortItem.setVisible(true);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        fabSimple.hide();
                        return true; // Return true to expand action view
                    }
                });

        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // White color for searchView
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));

        searchView.setQueryHint(getString(R.string.toolbar_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.initiateOriginalList();
                sortItem.setVisible(false);
            }
        });

        String currentSort = mApp.getPref(mApp.CURRENT_SIMPLE_SORT, mApp.SORT_BY_DATE);
        int direction = mApp.getPref(mApp.CURRENT_SIMPLE_SORT_DIRECTION, mApp.SORT_DEFAULT); // down default

        if (currentSort.equals(mApp.SORT_BY_NAME)) {
            if (direction == 0) {
                mi_sortByName.setIcon(downArrow);
            } else {
                mi_sortByName.setIcon(upArrow);
            }
        } else if (currentSort.equals(mApp.SORT_BY_TYPE)) {
            if (direction == 0) {
                mi_sortByType.setIcon(downArrow);
            } else {
                mi_sortByType.setIcon(upArrow);
            }
        } else if (currentSort.equals(mApp.SORT_BY_DATE)) {
            if (direction == 0) {
                mi_sortByDate.setIcon(downArrow);
            } else {
                mi_sortByDate.setIcon(upArrow);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            // Back button 
            case android.R.id.home:
                adapter.notifyDataSetChanged();
                break;
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.about:
                mApp.buildBasicDialog(MainActivity.this,
                        "In construction", "..", null, 0).show();
                break;
            case R.id.sort_by_name:
                updateSort(mApp.SORT_BY_NAME);
                break;
            case R.id.sort_by_type:
                updateSort(mApp.SORT_BY_TYPE);
                break;
            case R.id.sort_by_date:
                updateSort(mApp.SORT_BY_DATE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
