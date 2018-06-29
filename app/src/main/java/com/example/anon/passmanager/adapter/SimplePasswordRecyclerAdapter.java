package com.example.anon.passmanager.adapter;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anon.passmanager.App;
import com.example.anon.passmanager.R;
import com.example.anon.passmanager.activity.MainActivity;
import com.example.anon.passmanager.model.SimplePassword;
import com.example.anon.passmanager.util.Formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Created by Anon on 10/28/2016.
 */

public class SimplePasswordRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private PasswordAdapterCallback callback;
    private ArrayList<SimplePassword> mSimplePasswordList, originalList;

    public ArrayList<ViewHolder> viewHolderList = new ArrayList<>();
    public ArrayList<SimplePassword> selectedList = new ArrayList<>();

    private static final int EMPTY_VIEW = 10;

    App mApp = App.getAppInstance();
    int lastPosition = -1;

    MainActivity mainActivity;

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MainActivity mainActivity;
        RelativeLayout relativeCardview;
        CardView cardView;
        TextView createdDateLeft;
        me.grantland.widget.AutofitTextView sourceName;
        ImageView moreSettings, pwdType, selectedIV;
        SimplePassword selectedPwd;

        public ViewHolder(View view, MainActivity mainActivity) {
            super(view);
            sourceName = (me.grantland.widget.AutofitTextView) view.findViewById(R.id.sourceName);
            createdDateLeft = (TextView) view.findViewById(R.id.tv_createdDate_left);
            pwdType = (ImageView) view.findViewById(R.id.pwdType);
            relativeCardview = (RelativeLayout) view.findViewById(R.id.relative_cardview);
            moreSettings = (ImageView) view.findViewById(R.id.moreSettings);
            cardView = (CardView) view.findViewById(R.id.cardView);
            selectedIV = (ImageView) view.findViewById(R.id.selected);
            this.mainActivity = mainActivity;
        }
    }

    public SimplePasswordRecyclerAdapter(Context context, ArrayList<SimplePassword> simplePasswordList) {
        try {
            this.callback = ((PasswordAdapterCallback) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
        this.mainActivity = (MainActivity) context;
        this.mSimplePasswordList = simplePasswordList;
        this.originalList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSimplePasswordList.size() > 0 ? mSimplePasswordList.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mSimplePasswordList.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == EMPTY_VIEW) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.empty_view_layout, parent, false);
            EmptyViewHolder evh = new EmptyViewHolder(itemView);
            return evh;
        }
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.passcode_card, parent, false);
        ViewHolder myViewHolder = new ViewHolder(itemView, mainActivity);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            final SimplePassword simplePassword = mSimplePasswordList.get(holder.getAdapterPosition());
            final ViewHolder viewHolder = (ViewHolder) holder;
            
            viewHolder.selectedPwd = simplePassword;
            viewHolder.sourceName.setText(simplePassword.getAlias());
            viewHolder.createdDateLeft.setText(Formatter.formatAdaptableDate(simplePassword.getDate()));
            viewHolder.pwdType.setImageDrawable(ResourcesCompat.getDrawable(
                    mApp.getRes(), mApp.simple_type_items[simplePassword.getType() - 1].getDrawable(), null));

            viewHolder.pwdType.setVisibility((simplePassword.isSelected) ? View.GONE : View.VISIBLE);
            viewHolder.selectedIV.setVisibility((simplePassword.isSelected) ? View.VISIBLE : View.GONE);

            //viewHolder.moreSettings.setVisibility(View.VISIBLE);

            // Add on click listeners
            viewHolder.relativeCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mainActivity.selectionState) {
                        if (simplePassword.isSelected && selectedList.size() == 1) {
                            setSelected(viewHolder, simplePassword, false);
                            viewHolderList.remove(viewHolder);
                            mainActivity.toggleSelection(false);
                            return;
                        }
                        if (simplePassword.isSelected) {
                            setSelected(viewHolder, simplePassword, false);
                            viewHolderList.remove(viewHolder);
                        } else {
                            setSelected(viewHolder, simplePassword, true);
                            viewHolderList.add(viewHolder);
                        }
                    } else
                        callback.showViewPasswordActivitity(view, holder.getAdapterPosition());
                }
            });
            viewHolder.relativeCardview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!mainActivity.selectionState) {
                        mainActivity.toggleSelection(true);
                        setSelected(viewHolder, simplePassword, true);
                        viewHolderList.add(viewHolder);
                        return true;
                    } else {
                        return false;
                    }

                }
            });
            viewHolder.moreSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.showPopup(view, holder.getAdapterPosition());
                }
            });

            setAnimation(holder.itemView, position);
        }
    }

    public void deselectAll(boolean notify) {
        for (ViewHolder vh : viewHolderList) {
            setSelected(vh, vh.selectedPwd, false);
        }
        viewHolderList.clear();
        selectedList.clear();
        if (notify) notifyDataSetChanged();
    }

    public void setSelected(final ViewHolder holder, final SimplePassword selectedPwd, final boolean toSelectedState) {
        selectedPwd.isSelected = toSelectedState;
        //holder.relativeCardview.setBackgroundColor((toSelectedState) ? mApp.getRes().getColor(R.color.selectedRV) : 0);

        if (toSelectedState)
            selectedList.add(selectedPwd);
        else
            selectedList.remove(selectedPwd);

        if (selectedList.size() > 1)
            mainActivity.selectionToolbar.findViewById(R.id.selection_edit).setVisibility(View.GONE);
        else
            mainActivity.selectionToolbar.findViewById(R.id.selection_edit).setVisibility(View.VISIBLE);

        mainActivity.onChangeCounter(selectedList.size());
        holder.pwdType.setVisibility((toSelectedState) ? View.GONE : View.VISIBLE);
        holder.selectedIV.setVisibility((toSelectedState) ? View.VISIBLE : View.GONE);
    }

    public void filter(String text) {
        if (originalList.size() > 0) {
            mSimplePasswordList.clear();
            if (text.isEmpty()) {
                mSimplePasswordList.addAll(originalList);
            } else {
                text = text.toLowerCase();
                for (SimplePassword item : originalList) {
                    if (item.getAlias().toLowerCase().contains(text)) {
                        mSimplePasswordList.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    public void restoreAdapterList() {
        mSimplePasswordList.clear();
        mSimplePasswordList.addAll(originalList);
        originalList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void initiateOriginalList() {
        this.originalList = cloneList(mSimplePasswordList);
    }

    private ArrayList<SimplePassword> cloneList(List<SimplePassword> adapterList) {
        ArrayList<SimplePassword> clonedList = new ArrayList<>(adapterList.size());
        for (SimplePassword s : adapterList) {
            clonedList.add(new SimplePassword(s));
        }
        return clonedList;
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position < lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mainActivity.getApplicationContext(), R.anim.push_left_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void sortBy(String sort, int direction) {
        if (sort.equals(mApp.SORT_BY_NAME)) {
            if (direction == 1) {
                Collections.sort(this.mSimplePasswordList, new Comparator<SimplePassword>() {
                    public int compare(SimplePassword p1, SimplePassword p2) {
                        return p1.getAlias().compareTo(p2.getAlias());
                    }
                });
            } else {
                Collections.sort(this.mSimplePasswordList, new Comparator<SimplePassword>() {
                    public int compare(SimplePassword p1, SimplePassword p2) {
                        return p2.getAlias().compareTo(p1.getAlias());
                    }
                });
            }
        } else if (sort.equals(mApp.SORT_BY_TYPE)) {
            if (direction == 1) {
                Collections.sort(this.mSimplePasswordList, new Comparator<SimplePassword>() {
                    public int compare(SimplePassword p1, SimplePassword p2) {
                        return Integer.toString(p1.getType()).compareTo(Integer.toString(p2.getType()));
                    }
                });
            } else {
                Collections.sort(this.mSimplePasswordList, new Comparator<SimplePassword>() {
                    public int compare(SimplePassword p1, SimplePassword p2) {
                        return Integer.toString(p2.getType()).compareTo(Integer.toString(p1.getType()));
                    }
                });
            }
        } else if (sort.equals(mApp.SORT_BY_DATE)) {
            if (direction == 1) {
                Collections.sort(this.mSimplePasswordList, new Comparator<SimplePassword>() {
                    public int compare(SimplePassword p1, SimplePassword p2) {
                        return Long.toString(p1.getDate()).compareTo(Long.toString(p2.getDate()));
                    }
                });
            } else {
                Collections.sort(this.mSimplePasswordList, new Comparator<SimplePassword>() {
                    public int compare(SimplePassword p1, SimplePassword p2) {
                        return Long.toString(p2.getDate()).compareTo(Long.toString(p1.getDate()));
                    }
                });
            }
        }
        notifyDataSetChanged();
        //notifyAdapterSort();
    }

    public interface PasswordAdapterCallback {
        void showPopup(View view, int position);

        void showViewPasswordActivitity(View view, int position);
    }
}