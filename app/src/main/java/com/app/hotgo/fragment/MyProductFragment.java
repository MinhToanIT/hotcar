package com.app.hotgo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;
import com.app.hotgo.activities.MainActivity;
import com.app.hotgo.activities.ManageProductActivity;
import com.app.hotgo.activities.NewProductActivity;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.object.User;
import com.app.hotgo.widget.CircleImageView;

public class MyProductFragment extends BaseFragment implements View.OnClickListener {
    private View rootView;
    TextView tvName;
    CircleImageView ivAvatar;
    RatingBar ratingBar;
    private User user;
    private Button btnNewProduct, btnManagerProduct;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_my_product, container, false);
        initViews();
        initMenuButton(rootView);
        initData();
        initControl();
        return rootView;
    }


    private void initViews() {
        tvName = rootView.findViewById(R.id.tv_name);
        ivAvatar = rootView.findViewById(R.id.iv_avatar);
        ratingBar = rootView.findViewById(R.id.ratingBar);

        btnNewProduct = rootView.findViewById(R.id.btn_new_product);
        btnManagerProduct = rootView.findViewById(R.id.btn_manager_product);
    }

    protected void initMenuButton(View view) {
        view.findViewById(R.id.btnMenu).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) self).menu.showMenu();
                        // hide key
                        View view = self.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) self.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                });
    }

    private void initData() {
        user = GlobalValue.getInstance().getUser();
        tvName.setText(user.getFullName());
        Glide.with(self).load(user.getLinkImage()).into(ivAvatar);

        if (preferencesManager.isUser()) {
            if (user.getPassengerRate().length() == 0) {
                ratingBar.setRating(0);
            } else {
                ratingBar.setRating(Float.parseFloat(user.getPassengerRate()) / 2);
            }
        } else {
            if (user.getDriverObj() != null) {
                if (user.getDriverObj().getDriverRate() != null && user.getDriverObj().getDriverRate().length() != 0) {
                    ratingBar.setRating(Float.parseFloat(user.getDriverObj()
                            .getDriverRate()) / 2);
                } else {
                    ratingBar.setRating(0);
                }
            }
            if (user.getShopObj() != null) {
                if (user.getShopObj().getRate() != null && user.getShopObj().getRate().length() != 0) {
                    ratingBar.setRating(Float.parseFloat(user.getShopObj()
                            .getRate()) / 2);
                } else {
                    ratingBar.setRating(0);
                }
            }

        }
    }

    private void initControl() {
        btnNewProduct.setOnClickListener(this);
        btnManagerProduct.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_new_product:
                startActivity(NewProductActivity.class);
                break;
            case R.id.btn_manager_product:
                startActivity(ManageProductActivity.class);
                break;
        }
    }
}
