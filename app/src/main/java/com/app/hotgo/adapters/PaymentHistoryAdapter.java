package com.app.hotgo.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.hotgo.R;
import com.app.hotgo.object.ItemTransactionHistory;
import com.app.hotgo.utility.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PaymentHistoryAdapter extends BaseAdapter {

    private LayoutInflater mInflate;
    private ArrayList<ItemTransactionHistory> arrViews;
    Activity mAct;

    // AQuery aq;

    public PaymentHistoryAdapter(Activity activity,
                                 ArrayList<ItemTransactionHistory> arrViews) {
        this.mAct = activity;
        this.arrViews = arrViews;
        this.mInflate = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ArrayList<ItemTransactionHistory> getArrViews() {
        return arrViews;
    }

    public void setArrViews(ArrayList<ItemTransactionHistory> arrViews) {
        this.arrViews = arrViews;
    }

    @Override
    public int getCount() {

        return arrViews.size();
    }

    @Override
    public Object getItem(int position) {

        return position;
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
            convertView = mInflate.inflate(
                    R.layout.layout_item_payment_history, null);

            holder.lblTransactionID = convertView
                    .findViewById(R.id.txtTransactionID);
            holder.tvDate = convertView
                    .findViewById(R.id.tv_date);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.tvTime = convertView.findViewById(R.id.tv_time);

            holder.lblNote = convertView.findViewById(R.id.txtNote);
            holder.lblPoint = convertView
                    .findViewById(R.id.lblDestination);
            holder.txtTripID = convertView
                    .findViewById(R.id.txtTripID);
            holder.lblTripIdHead = convertView.findViewById(R.id.lblTripIdHead);

            convertView.setTag(holder);

        } else {
            holder = (HolderView) convertView.getTag();
        }
        ItemTransactionHistory itemTransaction = arrViews.get(position);
        if (itemTransaction != null) {
            holder.lblTransactionID.setText(itemTransaction.getTransactionId()
                    .toString());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

            holder.tvDate.setText(dateFormat.format(DateUtil.convertStringToTimeStamp(itemTransaction.getDateTimeTransaction(), "yyyy-MM-dd HH:mm:ss")));
            holder.tvTime.setText(timeFormat.format(DateUtil.convertStringToTimeStamp(itemTransaction.getDateTimeTransaction(), "yyyy-MM-dd HH:mm:ss")));

            if (itemTransaction.getTripId().equals("null")) {
//                holder.txtTripID.setVisibility(View.GONE);
                holder.txtTripID.setText(mAct.getResources().getString(R.string.no_information));
                //holder.lblTripIdHead.setVisibility(View.GONE);
            } else {
                holder.txtTripID.setText(itemTransaction.getTripId());
                //holder.lblTripIdHead.setVisibility(View.VISIBLE);
            }

            holder.lblPoint.setSelected(true);
            if (itemTransaction.getTypeTransaction().equalsIgnoreCase("-")) {
                holder.lblPoint.setText("-" + mAct.getResources().getString(R.string.lblCurrency) + itemTransaction.getPointTransaction());
                holder.lblPoint.setBackgroundResource(R.drawable.bg_border_red_right_top_right_bottom);
            } else {
                if (itemTransaction.getTypeTransaction().equalsIgnoreCase("+")) {
                    holder.lblPoint.setText("+" + mAct.getResources().getString(R.string.lblCurrency) + itemTransaction
                            .getPointTransaction());
                    holder.lblPoint.setBackgroundResource(R.drawable.bg_border_green_right_top_right_bottom);
                }

            }

            if (itemTransaction.getNoteTransaction().equals("1")) {
                holder.lblNote.setText(mAct.getResources().getString(
                        R.string.action_cancellation_order_fee));
            } else if (itemTransaction.getNoteTransaction().equals("2")) {
                holder.lblNote.setText(mAct.getResources().getString(
                        R.string.action_exchange_point));
            } else if (itemTransaction.getNoteTransaction().equals("3")) {
                holder.lblNote.setText(mAct.getResources().getString(
                        R.string.action_redeem_point));
            } else if (itemTransaction.getNoteTransaction().equals("4")) {
                holder.lblNote.setText(mAct.getResources().getString(
                        R.string.action_transfer_point));
            } else if (itemTransaction.getNoteTransaction().equals("5")) {
                holder.lblNote.setText(mAct.getResources().getString(
                        R.string.action_trip_payment));
            } else if (itemTransaction.getNoteTransaction().equals("6")) {
                holder.lblNote.setText(mAct.getResources().getString(
                        R.string.action_passenger_share_bonus));
            } else {
                holder.lblNote.setText(mAct.getResources().getString(
                        R.string.action_driver_share_bonus));
            }

        }
        return convertView;
    }

    public class HolderView {
        TextView lblPoint, lblTransactionID, lblNote,
                txtTripID, lblTripIdHead;
        private TextView tvDate, tvTime;
    }
}
