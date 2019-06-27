package com.example.ivangarrera.example.Controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivangarrera.example.R;

import java.util.ArrayList;

public class AlertListAdapter extends BaseAdapter {
    protected Activity activity;
    protected ArrayList<AlertListViewItem> items;

    public AlertListAdapter(Activity activity, ArrayList<AlertListViewItem> items) {
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
    @TargetApi(Build.VERSION_CODES.M)
    public View getView(int position, View convertView, ViewGroup parent) {
        View temp_view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            temp_view = inflater.inflate(R.layout.alert_list_item, null);
        }
        AlertListViewItem item = items.get(position);

        TextView tvCoord = temp_view.findViewById(R.id.il_coordinates);
        TextView tvTimestamp = temp_view.findViewById(R.id.il_timestamp);
        ImageView imageView = temp_view.findViewById(R.id.il_icon);

        tvCoord.setText(item.getCoordinates());
        tvTimestamp.setText(item.getTimestamp());
        imageView.setBackground(item.getImageView().getBackground());
        return temp_view;
    }
}
