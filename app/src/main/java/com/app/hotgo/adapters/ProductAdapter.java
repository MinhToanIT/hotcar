package com.app.hotgo.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.app.hotgo.R;
import com.app.hotgo.object.ProductObj;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private Activity context;
    private ArrayList<ProductObj> listProduct;
    private LayoutInflater inflater;
    private int quantity = 1;
    private OnBuyButtonClickListener onBuyButtonClickListener;

    public ProductAdapter(Activity context, ArrayList<ProductObj> listProduct) {
        this.context = context;
        this.listProduct = listProduct;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_list_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ProductObj productObj = listProduct.get(position);
        if (productObj != null) {
            holder.tvProductName.setText(productObj.getTitle());
            holder.tvDescription.setText(productObj.getDescription());
            holder.tvPrice.setText(context.getResources().getString(R.string.lblCurrency) + productObj.getPrice());
            holder.tvShopName.setText(productObj.getShopName());
            holder.tvShippingPrice.setText(" " + context.getResources().getString(R.string.lblCurrency) + productObj.getShipFee());
            holder.tvTotalPrice.setText(" " + context.getResources().getString(R.string.lblCurrency) + productObj.getTotalPrice());
            holder.tvAddress.setText(productObj.getAddress());
            holder.tvRate.setText(String.format("%.2f", Float.parseFloat(productObj.getRate()) / 2F) + " (" + productObj.getRateCount() + ")");

            Glide.with(context).load(productObj.getImage()).into(holder.ivProduct);

        }
        holder.tvIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity = Integer.parseInt(holder.tvQuantity.getText().toString().trim());
                quantity++;
                holder.tvQuantity.setText(String.valueOf(quantity));
                holder.tvTotalPrice.setText(context.getResources().getString(R.string.lblCurrency) + (Float.parseFloat(productObj.getPrice()) * quantity + Float.parseFloat(productObj.getShipFee())));
            }
        });
        holder.tvDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity = Integer.parseInt(holder.tvQuantity.getText().toString().trim());
                if (quantity > 1)
                    quantity--;
                holder.tvQuantity.setText(String.valueOf(quantity));
                holder.tvTotalPrice.setText(context.getResources().getString(R.string.lblCurrency) + (Float.parseFloat(productObj.getPrice()) * quantity + productObj.getShipFee()));
            }
        });
        holder.tvBuyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBuyButtonClickListener.onBuyButtonClick(position, quantity, Float.parseFloat(productObj.getPrice()) * quantity + Float.parseFloat(productObj.getShipFee()),productObj.getDistance());
            }
        });
        holder.ivCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"
                        + productObj.getPhone()));
                context.startActivity(callIntent);
            }
        });
        holder.ivSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", productObj.getPhone());
                smsIntent.putExtra("sms_body", "message");
                context.startActivity(smsIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listProduct.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProduct;
        private TextView tvProductName, tvDescription, tvPrice,tvAddress;
        private TextView tvShopName, tvRate, tvShippingPrice, tvTotalPrice;
        private ImageView ivCall, ivSms;
        private TextView tvIncrease, tvDecrease, tvQuantity, tvBuyNow;

        public ViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            ivCall = itemView.findViewById(R.id.iv_call);
            ivSms = itemView.findViewById(R.id.iv_sms);

            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvShopName = itemView.findViewById(R.id.tv_shop_name);
            tvRate = itemView.findViewById(R.id.tv_rate);
            tvShippingPrice = itemView.findViewById(R.id.tv_shipping_price);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvIncrease = itemView.findViewById(R.id.tv_increase);
            tvDecrease = itemView.findViewById(R.id.tv_decrease);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvBuyNow = itemView.findViewById(R.id.tv_buy_now);
            tvAddress = itemView.findViewById(R.id.tv_address);


        }
    }

    public interface OnBuyButtonClickListener {
        void onBuyButtonClick(int position, int quantity, float price, String distance);
    }

    public void setOnBuyButtonClickListener(OnBuyButtonClickListener onBuyButtonClickListener) {
        this.onBuyButtonClickListener = onBuyButtonClickListener;
    }
}
