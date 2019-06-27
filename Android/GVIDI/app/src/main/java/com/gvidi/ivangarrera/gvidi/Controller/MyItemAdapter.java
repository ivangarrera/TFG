package com.example.ivangarrera.example.Controller;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ivangarrera.example.R;

import java.util.ArrayList;

public class MyItemAdapter extends BaseAdapter {
    protected Activity activity;
    protected ArrayList<MyListViewItem> items;

    public MyItemAdapter(Activity activity, ArrayList<MyListViewItem> items) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View temp_view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            temp_view = inflater.inflate(R.layout.list_item, null);
        }

        MyListViewItem item = items.get(position);

        TextView tvExpName = temp_view.findViewById(R.id.expedition_name);
        TextView tvGuideName = temp_view.findViewById(R.id.guide_name);

        tvExpName.setText(item.getExpeditionName());
        tvGuideName.setText(item.getGuideName());
        return temp_view;
    }
}
