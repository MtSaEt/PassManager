package com.example.anon.passmanager.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anon.passmanager.R;
import com.example.anon.passmanager.helper.DatabaseController;
import com.example.anon.passmanager.util.OnSingleClickListener;

import java.util.List;

/**
 * Created by Anon on 2017-03-29.
 */

public class DialogArrayAdapter extends ArrayAdapter<String> {
    Context context;
    int resourceId;
    AutoCompleteTextView mAutoCompleteTextView;
    List<String> usernames;

    public DialogArrayAdapter(Context context, int resourceId, List<String> items, AutoCompleteTextView autoCompleteTextView) {
        super(context, resourceId, items);
        this.context = context;
        this.resourceId = resourceId;
        this.mAutoCompleteTextView = autoCompleteTextView;
        this.usernames = items;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final String username = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(this.resourceId, null);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.tv_view_username);
            holder.imageView = (ImageView) convertView.findViewById(R.id.iv_view_username);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtTitle.setText(username);
        holder.imageView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                DatabaseController.instance.deleteUsername(username);
                usernames.remove(position);
                mAutoCompleteTextView.dismissDropDown();
            }
        });
        return convertView;
    }
}