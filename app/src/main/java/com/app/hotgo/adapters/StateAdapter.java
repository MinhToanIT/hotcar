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
import com.app.hotgo.object.StateObj;
import com.app.hotgo.object.User;

public class StateAdapter extends BaseAdapter {

    // public ArrayList<NewsObj> arrNews;
    private LayoutInflater mInflate;
    private ArrayList<StateObj> arrViews;
    Activity mAct;
    User user;

    // AQuery aq;

    public StateAdapter(Activity activity, ArrayList<StateObj> arrViews) {
        this.mAct = activity;
        this.arrViews = arrViews;
        this.mInflate = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrViews.size();
    }

    public ArrayList<StateObj> getArrViews() {
        return arrViews;
    }

    public void setArrViews(ArrayList<StateObj> arrViews) {
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
        StateObj itemTripHistory = arrViews.get(position);

        if (itemTripHistory != null) {
            holder.txtState.setText(itemTripHistory.getStateName() + "");
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
        StateObj itemTripHistory = arrViews.get(position);

        if (itemTripHistory != null) {
            holder.txtState.setText(itemTripHistory.getStateName() + "");
        }
        return convertView;
    }
}
