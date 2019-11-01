package com.app.hotgo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;

public class ChangePasswordFragment extends BaseFragment {

    TextView txtTitle, txtWebsite;
    private EditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    private TextView btnSubmit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        initControl(view);
        initUI(view);
        initMenuButton(view);
        initData();
        setHeaderTitle(R.string.change_password);
        return view;
    }

    public void initData() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    changePassword(edtCurrentPassword.getText().toString().trim(), edtNewPassword.getText().toString().trim());
                }
            }
        });
    }

    private void initControl(View view) {
        txtTitle =  view.findViewById(R.id.lblTitle);
        txtWebsite =  view.findViewById(R.id.txtWebsite);
        edtCurrentPassword = view.findViewById(R.id.edt_current_password);
        edtNewPassword = view.findViewById(R.id.edt_new_password);
        edtConfirmPassword = view.findViewById(R.id.edt_confirm_password);
        btnSubmit = view.findViewById(R.id.bt_submit);
    }

    private boolean validate() {
        if (edtCurrentPassword.getText().toString().trim().isEmpty()) {
            showToast(getResources().getString(R.string.current_password_not_empty));
            edtCurrentPassword.requestFocus();
            return false;
        } else if (edtNewPassword.getText().toString().trim().isEmpty()) {
            showToast(getResources().getString(R.string.input_new_password));
            edtNewPassword.requestFocus();
            return false;
        } else if (edtConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast(getResources().getString(R.string.input_cofirm_password));
            edtConfirmPassword.requestFocus();
            return false;
        } else if (!edtNewPassword.getText().toString().trim().equals(edtConfirmPassword.getText().toString().trim())) {
            showToast(getResources().getString(R.string.password_mismatch));
            edtNewPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void changePassword(String oldPassword, String newPassword) {
        ModelManager.changePassword(self, preferencesManager.getToken(), oldPassword, newPassword, true, new ModelManagerListener() {
            @Override
            public void onError() {
                showToast(getResources().getString(R.string.message_have_some_error));
            }

            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    showToast(ParseJsonUtil.getMessage(json));
                    format();
                    mainActivity.setMenuByUserType();
                    mainActivity.showFragment(mainActivity.PASSENGER_PAGE1);
                } else {
                    showToast(ParseJsonUtil.getMessage(json));
                }
            }
        });
    }

    private void format() {
        edtCurrentPassword.setText("");
        edtNewPassword.setText("");
        edtConfirmPassword.setText("");
    }
}
