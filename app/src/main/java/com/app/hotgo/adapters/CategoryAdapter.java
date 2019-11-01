package com.app.hotgo.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.hotgo.R;
import com.app.hotgo.object.CarType;

import java.util.ArrayList;

public class CategoryAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<CarType> listCategory;
    private LayoutInflater inflater;

    public CategoryAdapter(Activity context, ArrayList<CarType> listCategory) {
        this.context = context;
        this.listCategory = listCategory;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listCategory.size();
    }

    @Override
    public Object getItem(int position) {
        return listCategory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_category, parent, false);
            holder = new ViewHolder();
            holder.tvCategory = convertView.findViewById(R.id.tv_category);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CarType carType = listCategory.get(position);
        holder.tvCategory.setText(carType.getName());
        return convertView;
    }

    class ViewHolder {
        TextView tvCategory;
    }
}
