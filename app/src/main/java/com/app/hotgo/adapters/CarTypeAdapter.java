package com.app.hotgo.adapters;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.app.hotgo.R;
import com.app.hotgo.object.CarType;

import java.util.List;

public class CarTypeAdapter extends RecyclerView.Adapter<CarTypeAdapter.MyViewHolder> {

    private List<CarType> moviesList;
    private static ClickListener clickListener;
    int positionCheck = 0;
    private Activity context;
    private int currentPosition;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public LinearLayout llTypeCar;
        public View viewBorder;
        private ImageView ivProductThumbnail;

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            title = view.findViewById(R.id.txtTitle);
            llTypeCar = view.findViewById(R.id.llTypeCar);
            viewBorder = view.findViewById(R.id.view);
            ivProductThumbnail = view.findViewById(R.id.iv_product_thumbnail);

            int widthScreen = context.getResources().getDisplayMetrics().widthPixels;
            int itemWidth = (int) (widthScreen / 2F);
            llTypeCar.setLayoutParams(new LinearLayout.LayoutParams(itemWidth - (int) (itemWidth / 12.5F), LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }


    public CarTypeAdapter(Activity context, List<CarType> moviesList) {
        this.context = context;
        this.moviesList = moviesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cartype, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        CarType movie = moviesList.get(position);
        holder.title.setText(movie.getName());
        holder.llTypeCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(position, v);
//                notifyDataSetChanged();

            }
        });
        if (currentPosition == position) {
            Glide.with(context).load(movie.getImageSelected()).into(holder.ivProductThumbnail);
            holder.title.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            Glide.with(context).load(movie.getImageType()).into(holder.ivProductThumbnail);
            holder.title.setTextColor(context.getResources().getColor(R.color.text_color_primary));
        }

    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    public int getPositionCheck() {
        return positionCheck;
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        notifyDataSetChanged();
    }
}