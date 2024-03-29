package com.app.hotgo.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.hotgo.R;
import com.app.hotgo.object.CityObj;
import com.app.hotgo.object.User;

public class CityAdapter extends BaseAdapter {

    // public ArrayList<NewsObj> arrNews;
    private LayoutInflater mInflate;
    private ArrayList<CityObj> arrViews;
    Activity mAct;
    User user;

    // AQuery aq;

    public CityAdapter(Activity activity, ArrayList<CityObj> arrViews) {
        this.mAct = activity;
        this.arrViews = arrViews;
        this.mInflate = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrViews.size();
    }

    public ArrayList<CityObj> getArrViews() {
        return arrViews;
    }

    public void setArrViews(ArrayList<CityObj> arrViews) {
        this.arrViews = arrViews;
    }

    @Override
    public Object getItem(int position) {

        return arrViews.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final HolderView holder;
        if (convertView == null) {
            holder = new HolderView();
            convertView = mInflate.inflate(R.layout.item_state, null);

            holder.txtState =  convertView.findViewById(R.id.txtState);

            convertView.setTag(holder);
        } else {
            holder = (HolderView) convertView.getTag();
        }
        CityObj itemTripHistory = arrViews.get(position);

        if (itemTripHistory != null) {
            holder.txtState.setText(itemTripHistory.getCityName() + "");
        }
        return convertView;
    }

    public class HolderView {
        TextView txtState;
    }

    public class HolderViewDrop {
        TextView txtState;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        final HolderViewDrop holder;
        if (convertView == null) {
            holder = new HolderViewDrop();
            convertView = mInflate.inflate(R.layout.item_state_drop, null);

            holder.txtState =  convertView.findViewById(R.id.txtStateDrop);

            convertView.setTag(holder);
        } else {
            holder = (HolderViewDrop) convertView.getTag();
        }
        CityObj itemTripHistory = arrViews.get(position);

        if (itemTripHistory != null) {
            holder.txtState.setText(itemTripHistory.getCityName() + "");
        }
        return convertView;
    }
}
