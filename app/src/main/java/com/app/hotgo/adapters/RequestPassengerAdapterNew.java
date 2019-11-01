package com.app.hotgo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.bumptech.glide.Glide;
import com.app.hotgo.R;
import com.app.hotgo.object.RequestObj;

import java.util.ArrayList;

public class RequestPassengerAdapterNew extends BaseAdapter {
    private final AQuery aq;
    private Context context;
    private ArrayList<RequestObj> array;
    private LayoutInflater inflater;
    IListennerAdapter iListennerAdapter;
    private OnClickDeleteButtonListener onClickDeleteButtonListener;

    public interface IListennerAdapter {
        void onClickItemAdapter(int position);
    }

    public interface OnClickDeleteButtonListener {
        void onClickDelete(int position);
    }

    public RequestPassengerAdapterNew(Context context, ArrayList<RequestObj> array, IListennerAdapter iListennerAdapter, OnClickDeleteButtonListener onClickDeleteButtonListener) {
        this.context = context;
        this.array = array;
        aq = new AQuery(context);
        this.iListennerAdapter = iListennerAdapter;
        this.onClickDeleteButtonListener = onClickDeleteButtonListener;
        inflater = LayoutInflater.from(context);
    }

    public void setArrViews(ArrayList<RequestObj> array) {
        this.array = array;
    }

    public void updateResults(ArrayList<RequestObj> results) {
        this.array = results;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return array.size();
    }

    @Override
    public Object getItem(int position) {
        return array.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_list_request,
                    parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tv_name);
            holder.tvPhoneNumber = convertView.findViewById(R.id.tv_phone_number);
            holder.tvStar = convertView.findViewById(R.id.tv_start);
            holder.tvShopName = convertView.findViewById(R.id.tv_shop_name);
            holder.tvProductName = convertView.findViewById(R.id.tv_product_name);
            holder.tvSize = convertView.findViewById(R.id.tv_size);
            holder.tvAddress = convertView.findViewById(R.id.tv_address);
            holder.tvDestination = convertView.findViewById(R.id.tv_destination);
            holder.tvShippingPrice = convertView.findViewById(R.id.tv_shipping_price);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RequestObj obj = array.get(position);
        holder.tvName.setText(obj.getPassengerName());
        holder.tvPhoneNumber.setText(obj.getPassengerphone());
//        holder.tvShopName.setText(obj.getS);
//        holder.tvProductName.setText(obj.getS);

        Glide.with(context).load(obj.getPassengerImage()).into(holder.imgPassenger);
        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDeleteButtonListener.onClickDelete(position);
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iListennerAdapter.onClickItemAdapter(position);
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView tvName, tvPhoneNumber, tvStar;
        TextView tvShopName, tvProductName, tvSize, tvAddress, tvDestination, tvShippingPrice;
        ImageView imgPassenger, ivDelete;
    }

    public int convertToInt(String s) {
        switch (s) {
            case "I":
                return 1;

            case "II":
                return 2;

            case "III":
                return 3;
            case "IV":
                return 4;
            case "V":
                return 5;


        }
        return 0;
    }
}
