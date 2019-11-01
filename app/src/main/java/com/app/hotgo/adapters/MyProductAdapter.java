package com.app.hotgo.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.app.hotgo.R;
import com.app.hotgo.activities.EditProductActivity;
import com.app.hotgo.config.Constant;
import com.app.hotgo.object.ProductObj;
import com.joooonho.SelectableRoundedImageView;

import java.util.ArrayList;

public class MyProductAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<ProductObj> listProduct;
    private LayoutInflater inflater;

    public MyProductAdapter(Activity context, ArrayList<ProductObj> listProduct) {
        this.context = context;
        this.listProduct = listProduct;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listProduct.size();
    }

    @Override
    public Object getItem(int position) {
        return listProduct.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_my_product, parent, false);
            holder = new ViewHolder();
            holder.tvProductName = convertView.findViewById(R.id.tv_product_name);
            holder.tvPrice = convertView.findViewById(R.id.tv_price);
            holder.ivProduct = convertView.findViewById(R.id.iv_product);
            holder.ivEdit = convertView.findViewById(R.id.iv_edit);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final ProductObj productObj = listProduct.get(position);
        holder.tvProductName.setText(productObj.getTitle());
        holder.tvPrice.setText(context.getResources().getString(R.string.lblCurrency) + productObj.getPrice());
        Glide.with(context).load(productObj.getImage()).into(holder.ivProduct);

        holder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra(Constant.PRODUCT, productObj);
                intent.putExtra(Constant.POSITION, position);
                context.startActivity(intent);
                context.overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_left);
            }
        });
        return convertView;
    }

    class ViewHolder {
        private ImageView ivEdit;
        private SelectableRoundedImageView ivProduct;
        private TextView tvProductName;
        private TextView tvPrice;
    }
}
