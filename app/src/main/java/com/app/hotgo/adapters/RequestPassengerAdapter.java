package com.app.hotgo.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.app.hotgo.R;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.object.RequestObj;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;

public class RequestPassengerAdapter extends BaseAdapter {
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

    public RequestPassengerAdapter(Context context, ArrayList<RequestObj> array, IListennerAdapter iListennerAdapter, OnClickDeleteButtonListener onClickDeleteButtonListener) {
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
            convertView = inflater.inflate(R.layout.custom_request_passenger,
                    parent, false);
            holder = new ViewHolder();

            holder.lblStartingLocation = convertView
                    .findViewById(R.id.lblStartingLocation);

            holder.lblEndingLocation = convertView
                    .findViewById(R.id.lblEndingLocation);
            holder.ratingBar = (RatingBar) convertView
                    .findViewById(R.id.ratingBar);
            holder.tvName = convertView.findViewById(R.id.tvName);
            holder.tvPhone = convertView.findViewById(R.id.tvPhone);
            holder.imgPassenger = convertView.findViewById(R.id.imgPassenger);
            holder.ivDelete = convertView.findViewById(R.id.iv_delete);
            holder.tvShopName = convertView.findViewById(R.id.tv_shop_name);
            holder.tvQuantity = convertView.findViewById(R.id.tv_quantity);
            holder.tvPrice = convertView.findViewById(R.id.tv_price);
            holder.tvSize = convertView.findViewById(R.id.tv_size);
            holder.tvStar = convertView.findViewById(R.id.tv_star);
            holder.ivCall = convertView.findViewById(R.id.iv_call);
            holder.ivSms = convertView.findViewById(R.id.iv_sms);
            holder.tvDistance = convertView.findViewById(R.id.tv_distance);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        final RequestObj obj = array.get(position);
        holder.lblStartingLocation.setText(obj.getStartLocation());
        holder.lblEndingLocation.setText(obj.getEndLocation());
        holder.tvStar.setText(Float.parseFloat(obj
                .getPassengerRate()) / 2 + " (" + obj.getRateCount() + ")");
        if (obj.getPassengerRate().isEmpty()) {
            holder.ratingBar.setRating(0);
        } else {
            holder.ratingBar.setRating(Float.parseFloat(obj
                    .getPassengerRate()) / 2);
        }
        holder.tvName.setText(obj.getPassengerName() + " ");
        String passengerPhone;
        if (!obj.getPassengerphone().isEmpty() && obj.getPassengerphone().length() > 6) {
            passengerPhone = obj.getPassengerphone().substring(0, obj.getPassengerphone().length() - 6) + " xxx xxx";
        } else {
            passengerPhone = obj.getPassengerphone();
        }
        holder.tvPhone.setText(passengerPhone);
        aq.id(holder.imgPassenger).image(obj.getPassengerImage());
        holder.tvShopName.setText(obj.getCategoryName());
        holder.tvPrice.setText( obj.getPrice() +context.getResources().getString(R.string.lblCurrency));
        holder.tvDistance.setText(obj.getDistance() + " Km");


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
        holder.ivCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"
                        + obj.getPassengerphone()));
                context.startActivity(callIntent);
            }
        });
        holder.ivSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", obj.getPassengerphone());
                smsIntent.putExtra("sms_body", "message");
                context.startActivity(smsIntent);
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView lblStartingLocation;
        TextView lblEndingLocation, lblEstimateFare, txtIdentity, txtCarPlate;
        RatingBar ratingBar;
        TextView tvName, tvSeat, tvPhone;
        ImageView ivDelete, ivCall, ivSms;
        SelectableRoundedImageView imgPassenger;
        TextView tvShopName, tvProductName, tvQuantity, tvPrice, tvSize;
        TextView tvShippingPrice, tvStar, tvDistance, tvTotalPrice;
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
