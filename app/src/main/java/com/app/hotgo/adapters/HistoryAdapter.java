package com.app.hotgo.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.hotgo.R;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.ItemTripHistory;
import com.app.hotgo.object.User;
import com.app.hotgo.utility.DateUtil;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HistoryAdapter extends BaseAdapter {

    // public ArrayList<NewsObj> arrNews;
    private LayoutInflater mInflate;
    private ArrayList<ItemTripHistory> arrViews;
    Activity mAct;
    User user;

    // AQuery aq;

    public HistoryAdapter(Activity activity, ArrayList<ItemTripHistory> arrViews) {
        this.mAct = activity;
        this.arrViews = arrViews;
        this.mInflate = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrViews.size();
    }

    public ArrayList<ItemTripHistory> getArrViews() {
        return arrViews;
    }

    public void setArrViews(ArrayList<ItemTripHistory> arrViews) {
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
    public View getView(final int position, View convertView, final ViewGroup parent) {

        getDataFromGlobal();
        final HolderView holder;
        if (convertView == null) {
            holder = new HolderView();
            convertView = mInflate.inflate(R.layout.layout_item_history, null);

            holder.tripId = convertView.findViewById(R.id.txtTripId);
            holder.tvDate = convertView
                    .findViewById(R.id.tv_date);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.departure = convertView.findViewById(R.id.txtPlaceGo);
            holder.destination = convertView.findViewById(R.id.txtDestination);
            holder.totalTime = convertView.findViewById(R.id.txtTime);
            holder.totalDistance = convertView.findViewById(R.id.txtLength);
            holder.totalPoint = convertView.findViewById(R.id.txtTotalPoint);
            holder.totalPoint.setSelected(true);
            holder.txtLinkTrip = convertView.findViewById(R.id.txtLinkTrip);
            holder.txtPaymentMethod = convertView.findViewById(R.id.txtPaymentMethod);

            holder.tvCategory = convertView.findViewById(R.id.tv_category);
            //holder.tvProductName = convertView.findViewById(R.id.tv_product_name);
            holder.tvPrice = convertView.findViewById(R.id.tv_price);
            holder.tvShopName = convertView.findViewById(R.id.tv_shop);
            holder.tvAddress = convertView.findViewById(R.id.tv_address);
            holder.tvBuyer = convertView.findViewById(R.id.tv_buyer);
            holder.tvDestination = convertView.findViewById(R.id.tv_destination);
            holder.tvPickUp = convertView.findViewById(R.id.tv_PickUp);
            holder.tvShipper = convertView.findViewById(R.id.tv_shipper_name);
            holder.tvShippingPrice = convertView.findViewById(R.id.tv_shipping_price);
            holder.tvDistance = convertView.findViewById(R.id.tv_distance);
            holder.tvStatus = convertView.findViewById(R.id.tv_status);
            holder.tvSubmit = convertView.findViewById(R.id.tv_submit);
            holder.tvQuantity = convertView.findViewById(R.id.tv_quantity);

            holder.layoutShipper = convertView.findViewById(R.id.layout_shipper);
            holder.layoutBuyer = convertView.findViewById(R.id.layout_buyer);
            holder.layoutRate = convertView.findViewById(R.id.layout_rate);


            convertView.setTag(holder);
        } else {
            holder = (HolderView) convertView.getTag();
        }
        final ItemTripHistory itemTripHistory = arrViews.get(position);

        if (itemTripHistory != null) {

            holder.ratingBar = convertView.findViewById(R.id.ratingBar);

            if (user.getIdUser().equals(itemTripHistory.getDriverId() + "")) {
                holder.layoutShipper.setVisibility(View.GONE);
                holder.layoutBuyer.setVisibility(View.VISIBLE);
                holder.layoutRate.setVisibility(View.GONE);
            } else {
                holder.layoutShipper.setVisibility(View.VISIBLE);
                holder.layoutBuyer.setVisibility(View.GONE);
                holder.layoutRate.setVisibility(View.GONE);
            }

            holder.tvSubmit.setVisibility(itemTripHistory.getIsRate().equals("1") ? View.INVISIBLE : View.VISIBLE);
            holder.ratingBar.setIsIndicator(itemTripHistory.getIsRate().equals("1") ? true : false);
            holder.tripId.setText(itemTripHistory.getTripId() + "");
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

            holder.tvDate.setText(dateFormat.format(DateUtil.convertStringToTimeStamp(itemTripHistory.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
            holder.tvTime.setText(timeFormat.format(DateUtil.convertStringToTimeStamp(itemTripHistory.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
            holder.departure.setText(itemTripHistory.getStartLocaton());

            holder.tvCategory.setText(itemTripHistory.getCategory());
            holder.tvPrice.setText(itemTripHistory.getPrice() + mAct.getResources().getString(R.string.lblCurrency) );
            holder.tvShopName.setText(itemTripHistory.getShopName());
            holder.tvAddress.setText(itemTripHistory.getShopAddress());
            holder.tvBuyer.setText(itemTripHistory.getBuyer());
            holder.tvDestination.setText(itemTripHistory.getEndLocation());
            holder.tvPickUp.setText(itemTripHistory.getStartLocaton());
            holder.tvShipper.setText(itemTripHistory.getShipper());
            holder.tvShippingPrice.setText(mAct.getResources().getString(R.string.lblCurrency) + itemTripHistory.getShippingPrice());
            holder.tvDistance.setText(itemTripHistory.getDistance() + " Km" + " (" + itemTripHistory.getTotalTime() + "phút)");
            holder.tvStatus.setText(/*itemTripHistory.getStatus().equals("1") ? */mAct.getResources().getString(R.string.completed) /*: mAct.getResources().getString(R.string.cancel)*/);
            holder.ratingBar.setRating(Float.parseFloat(itemTripHistory.getRateProduct()) / 2);
            holder.tvQuantity.setText("x" + itemTripHistory.getQuantity());
            if (itemTripHistory.getStartTimeWorking() != null && !itemTripHistory.getStartTimeWorking().equals("") && !itemTripHistory.getStartTimeWorking().equals("0")) {
                holder.totalDistance.setText(getTime(Long.parseLong(itemTripHistory.getStartTimeWorking()), Long.parseLong(itemTripHistory.getEndTimeWorking())));
            } else {
                holder.totalDistance.setText("0");
            }
            holder.destination.setText(itemTripHistory.getEndLocation());
            if (itemTripHistory.getPaymentMethod().equals("1")) {
                holder.txtPaymentMethod.setText(mAct.getString(R.string.pay_by_paypal));
            } else {
                holder.txtPaymentMethod.setText(mAct.getString(R.string.lbl_paybycash));
            }
            if ((itemTripHistory.getDriverId() + "").equals(PreferencesManager.getInstance(mAct).getUserID())) {
                if (itemTripHistory.getPaymentMethod().equals("2")) {
                    holder.totalPoint.setText("+" +itemTripHistory.getTotalPrice() + mAct.getResources().getString(R.string.lblCurrency)  );
                    holder.totalPoint.setBackgroundResource(R.drawable.bg_border_violet);
                } else {
                    holder.totalPoint.setText("+" + itemTripHistory.getTotalPrice() +mAct.getResources().getString(R.string.lblCurrency) );
                    holder.totalPoint.setBackgroundResource(R.drawable.bg_border_violet);
                }
            } else {
                holder.totalPoint.setText("-"  + itemTripHistory.getTotalPrice()+ mAct.getResources().getString(R.string.lblCurrency));
                holder.totalPoint.setBackgroundResource(R.drawable.bg_border_violet);
            }

            holder.totalTime.setText(String.valueOf(itemTripHistory.getDistance()) + "Km" + "(" + itemTripHistory.getTotalTime() + " minutes)");
            holder.txtLinkTrip.setText(convertLinkToString(itemTripHistory.getLink()) + "");

            holder.tvSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.ratingBar.getRating() != 0) {
                        rateProduct(position, itemTripHistory, itemTripHistory.getTripId() + "", holder.ratingBar.getRating() * 2 + "");
                    } else {
                        Toast.makeText(mAct, mAct.getResources().getString(R.string.please_enter_rate), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return convertView;
    }

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
    }

    public class HolderView {
        ImageView imgNews;
        TextView tripId, timeEnd, departure, destination,
                totalTime, totalDistance, totalPoint, txtLinkTrip, txtPaymentMethod;
        TextView tvCategory, tvProductName, tvPrice, tvShopName, tvBuyer, tvShipper, tvShippingPrice;
        TextView tvAddress, tvDestination, tvDistance, tvStatus,tvPickUp;
        LinearLayout layoutRate, layoutShipper, layoutBuyer;
        TextView tvSubmit;
        TextView tvDate, tvTime, tvQuantity;
        RatingBar ratingBar;
    }

    public String convertLinkToString(String link) {
        for (int i = 0; i < GlobalValue.getInstance().getListCarTypes().size(); i++) {
            if (GlobalValue.getInstance().getListCarTypes().get(i).getId() != null && !GlobalValue.getInstance().getListCarTypes().get(i).getId().equals("")) {
                if (GlobalValue.getInstance().getListCarTypes().get(i).getId().equals(link)) {
                    return GlobalValue.getInstance().getListCarTypes().get(i).getName();
                }
            }
        }
        return link;
    }

    public String getTime(long startTime, long endTime) {
        DateTime today = new DateTime(endTime * 1000L);
        DateTime yesterday = new DateTime(startTime * 1000L);
        Duration duration = new Duration(yesterday, today);
        long timeInMilliseconds = duration.getStandardSeconds();
        long mins = timeInMilliseconds / 60;
        long hour = mins / 60;
        if (mins < 1) {
            return "00 " + mAct.getResources().getString(R.string.minute);
        } else {
            if (hour < 1) {
                return mins + " " + mAct.getResources().getString(R.string.minute1);
            } else {
                return hour + " " + mAct.getResources().getString(R.string.hour) + mins + " " + mAct.getResources().getString(R.string.minute1);
            }
        }
    }

    private int convertToInt(String s) {
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

    private void rateProduct(int position, final ItemTripHistory itemTripHistory, String tripId, final String rate) {
        ModelManager.rateProduct(mAct, PreferencesManager.getInstance(mAct).getToken(), tripId, rate, true, new ModelManagerListener() {
            @Override
            public void onError() {
                Toast.makeText(mAct, mAct.getResources().getString(R.string.message_have_some_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    Toast.makeText(mAct, ParseJsonUtil.getMessage(json), Toast.LENGTH_SHORT).show();
                    itemTripHistory.setIsRate("1");
                    itemTripHistory.setRateProduct(rate);
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(mAct, ParseJsonUtil.getMessage(json), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
