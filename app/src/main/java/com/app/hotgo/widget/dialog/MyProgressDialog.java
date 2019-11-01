package com.app.hotgo.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import com.app.hotgo.R;

public class MyProgressDialog extends Dialog {

    public MyProgressDialog(Context context) {
        super(context);
    }


     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(
                 R.drawable.bg_dialog_progress);
        setContentView(R.layout.layout_progress_dialog);

    }

}
