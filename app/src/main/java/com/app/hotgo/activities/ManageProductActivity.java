package com.app.hotgo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.adapters.MyProductAdapter;
import com.app.hotgo.config.Constant;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.ProductObj;
import com.app.hotgo.widget.pulltorefresh.PullToRefreshBase;
import com.app.hotgo.widget.pulltorefresh.PullToRefreshGridView;

import java.util.ArrayList;

public class ManageProductActivity extends BaseActivity {
    private ImageButton btnBack;
    private ImageView ivSearch;
    private EditText edtSearchKey;

    private PullToRefreshGridView grvProduct;
    private GridView grvActually;
    private ArrayList<ProductObj> listProduct = new ArrayList<>();
    private MyProductAdapter myProductAdapter;

    private int page = 0;
    private boolean isLoadMore = true;

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter filter;
    private String searchKey = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_product);
        initViews();
        initData();
        initControl();
        mListener();
    }

    private void mListener() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constant.REFRESH)) {
                    getListProduct(searchKey, true, true);
                }
            }
        };
        filter = new IntentFilter();
        filter.addAction(Constant.REFRESH);
        registerReceiver(broadcastReceiver, filter);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        ivSearch = findViewById(R.id.iv_search);
        edtSearchKey = findViewById(R.id.edt_search_key);

        grvProduct = findViewById(R.id.grv_product);
        grvActually = grvProduct.getRefreshableView();

    }

    private void initData() {
        myProductAdapter = new MyProductAdapter(self, listProduct);
        grvActually.setAdapter(myProductAdapter);

        getListProduct(searchKey, true, false);
    }

    private void initControl() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        edtSearchKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchKey = s.toString();
                if (s.length() == 0) {
                    page = 0;
                    getListProduct(searchKey, true, false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtSearchKey.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    page = 0;
                    getListProduct(searchKey, true, false);
                    return true;
                }
                return false;
            }
        });
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = 0;
                getListProduct(searchKey, true, false);
            }
        });
        grvProduct.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                String label = DateUtils.formatDateTime(self,
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                getListProduct(searchKey, true, true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                getListProduct(searchKey, false, true);

            }
        });
    }

    private void getListProduct(String searchKey, final boolean isRefresh, final boolean isPull) {
        if (isRefresh) {
            isLoadMore = true;
            listProduct.clear();
            page = 0;
        }

        if (!isLoadMore) {
            showNoMoreData();
        } else {
            page++;
            ModelManager.getListProduct(preferencesManager.getToken(), searchKey, page + "", self, !isPull, new ModelManagerListener() {
                @Override
                public void onError() {
                    showToastMessage(getResources().getString(R.string.message_have_some_error));
                }

                @Override
                public void onSuccess(String json) {
                    if (ParseJsonUtil.isSuccess(json)) {
                        ArrayList<ProductObj> arrProduct = ParseJsonUtil.parseListProduct(json);
                        if (arrProduct.size() > 0) {
                            listProduct.addAll(ParseJsonUtil.parseListProduct(json));
                            isLoadMore = true;
                        } else {
                            isLoadMore = false;
                            showToastMessage(getResources().getString(R.string.message_have_no_more_data));
                        }

                        myProductAdapter.notifyDataSetChanged();
//                        grvActually.setSelection(listProduct.size() - 1);
                        grvProduct.onRefreshComplete();
                    } else {
                        showToastMessage(ParseJsonUtil.getMessage(json));
                    }
                }
            });
        }

    }

    private void showNoMoreData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showToastMessage(R.string.message_have_no_more_data);
                grvProduct.onRefreshComplete();
            }
        }, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
