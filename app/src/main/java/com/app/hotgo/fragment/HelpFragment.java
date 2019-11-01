package com.app.hotgo.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;

public class HelpFragment extends BaseFragment {

    TextView txtTitle, txtWebsite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        initControl(view);
        initUI(view);
        initMenuButton(view);
        initData();
        setHeaderTitle(R.string.lbl_help);
        return view;
    }

    public void initData() {
        txtWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"));
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initControl(View view) {
        txtTitle =  view.findViewById(R.id.lblTitle);
        txtWebsite =  view.findViewById(R.id.txtWebsite);
    }

    public void changeLanguage() {
        txtTitle.setText(R.string.lbl_help);
    }

}
