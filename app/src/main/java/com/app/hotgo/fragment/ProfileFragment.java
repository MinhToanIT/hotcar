package com.app.hotgo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.bumptech.glide.Glide;
import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;
import com.app.hotgo.R.id;
import com.app.hotgo.activities.UpdateProFileActivity;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.User;
import com.app.hotgo.widget.CircleImageView;
import com.joooonho.SelectableRoundedImageView;

public class ProfileFragment extends BaseFragment {
    public static final int REQUEST_CODE_INPUT = 1;
    public static final int RESULT_CODE_SAVE = 2;
    ImageView btnUpdate;
    TextView lblNameDriver;
    CircleImageView profile;
    TextView lblPhone, lblEmail, lblAddress;
    LinearLayout imgCar, llPhone;
    RatingBar ratingBar;

    User user = new User();
    private AQuery lstAq;
    static AQuery aq;
    private View view;

    private TextView tvDescription, tvTypeVehicle, tvVehiclePlate;
    private LinearLayout /*layoutTypeVehicle, layoutVehiclePlate,*/ layoutDescription, layoutVehicleInfo, layoutImage;
    private SelectableRoundedImageView ivOne, ivTwo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        preferencesManager = PreferencesManager.getInstance(self);
        Log.e("ee", "onResume");
        initUI(view);
        initControl();
        typeUser();
        initMenuButton(view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("ee", "onPause");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("ee", "onStart");
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getData();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getData();
        }
    }


    public void initUI(View view) {

        btnUpdate = view.findViewById(id.btn_edit);
        lblNameDriver = view.findViewById(R.id.lblNameDriver);
        profile = view.findViewById(R.id.imgProfile);
        lblPhone = view.findViewById(id.lblPhone);
        lblEmail = view.findViewById(R.id.lblEmail);
        lblAddress = view.findViewById(R.id.lblAddress);
        llPhone = view.findViewById(R.id.llPhone);
        ratingBar = view.findViewById(R.id.ratingBar);

        tvDescription = view.findViewById(id.tv_description);
        tvTypeVehicle = view.findViewById(id.tv_type_vehicle);
        tvVehiclePlate = view.findViewById(id.tv_vehicle_plate);

        layoutDescription = view.findViewById(id.layout_description);
        layoutVehicleInfo = view.findViewById(id.layout_vehicle_info);
        layoutImage = view.findViewById(id.layout_image);

        ivOne = view.findViewById(id.imgPhotoOne);
        ivTwo = view.findViewById(id.imgPhotoTwo);

//        typeUser();
        lstAq = new AQuery(self);
        aq = lstAq.recycle(view);
    }

    public void typeUser() {
        if (preferencesManager.isShop()) {

            layoutVehicleInfo.setVisibility(View.VISIBLE);
            layoutImage.setVisibility(View.VISIBLE);
        } else if (preferencesManager.isDriver()) {
            layoutVehicleInfo.setVisibility(View.VISIBLE);
            layoutImage.setVisibility(View.VISIBLE);
        } else {
            layoutDescription.setVisibility(View.VISIBLE);
            layoutVehicleInfo.setVisibility(View.GONE);
            layoutImage.setVisibility(View.GONE);
        }

    }

    public void initControl() {

        btnUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (preferencesManager.isUser()) {
                    mainActivity.gotoActivity(UpdateProFileActivity.class);
                } else {
                    if (preferencesManager.isActiveDriver() || preferencesManager.isActiveShop()) {
                        mainActivity.gotoActivity(UpdateProFileActivity.class);
                    } else {
                        showToast(R.string.message_your_account_in_wait);
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_INPUT) {

        }
    }

    // set data user
    private void setData() {
        if (user != null) {
            lblNameDriver.setText(user.getFullName());
            lblEmail.setText(user.getEmail());
            lblPhone.setText(user.getPhone());
            lblAddress.setText(user.getAddress());
            tvDescription.setText(user.getDescription());
        }
        if (preferencesManager.isUser()) {
            if (user.getPassengerRate().length() == 0) {
                ratingBar.setRating(0);
            } else {
                ratingBar.setRating(Float.parseFloat(user.getPassengerRate()) / 2);
            }
        } else {
            if (preferencesManager.isDriver()) {
                if (user.getDriverObj() != null) {
                    if (user.getDriverObj().getDriverRate() != null && user.getDriverObj().getDriverRate().length() != 0) {
                        ratingBar.setRating(Float.parseFloat(user.getDriverObj()
                                .getDriverRate()) / 2);
                    } else {
                        ratingBar.setRating(0);
                    }
                    tvTypeVehicle.setText(getResources().getString(R.string.type_of_vehicle) + ": " + user.getCarObj().getVehicleType());
                    tvVehiclePlate.setText(getResources().getString(R.string.vehicle_plate) + ": " + user.getCarObj().getVehiclePlate());
                    Glide.with(self).load(user.getCarObj().getImageone()).into(ivOne);
                    Glide.with(self).load(user.getCarObj().getImagetwo()).into(ivTwo);
                }
            } else {
                if (user.getShopObj() != null) {
                    Log.e("LUAN","đã vào day");
                    if (user.getShopObj().getRate() != null && user.getShopObj().getRate().length() != 0) {
                        ratingBar.setRating(Float.parseFloat(user.getShopObj()
                                .getRate()) / 2);
                    } else {
                        ratingBar.setRating(0);
                    }
                    Glide.with(self).load(user.getShopObj().getImageExtra()).into(ivOne);
                    Glide.with(self).load(user.getShopObj().getImageExtra2()).into(ivTwo);
                    tvTypeVehicle.setText(getResources().getString(R.string.type_of_vehicle) + ": " + user.getShopObj().getCarType());
                    tvVehiclePlate.setText(getResources().getString(R.string.vehicle_plate) + ": " + user.getShopObj().getCarPlate());
                }
            }

        }
    }

    // get data info user
    public void getData() {
        ModelManager.showInfoProfile(
                PreferencesManager.getInstance(mainActivity).getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            GlobalValue.getInstance().setUser(
                                    ParseJsonUtil.parseInfoProfile(json));
                            typeUser();
                            user = ParseJsonUtil.parseInfoProfile(json);
                            aq.id(profile).image(user.getLinkImage());
                            setData();

                        } else {
                            showToast(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToast(getResources().getString(
                                R.string.message_have_some_error));
                    }
                });
    }


}
