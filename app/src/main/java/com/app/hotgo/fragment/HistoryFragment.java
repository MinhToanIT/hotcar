package com.app.hotgo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.app.hotgo.BaseFragment;
import com.app.hotgo.R;
import com.app.hotgo.adapters.HistoryAdapter;
import com.app.hotgo.config.PreferencesManager;
import com.app.hotgo.modelmanager.ModelManager;
import com.app.hotgo.modelmanager.ModelManagerListener;
import com.app.hotgo.modelmanager.ParseJsonUtil;
import com.app.hotgo.object.ItemTripHistory;
import com.app.hotgo.widget.pulltorefresh.PullToRefreshBase;
import com.app.hotgo.widget.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.app.hotgo.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;

public class HistoryFragment extends BaseFragment {

	PullToRefreshListView lsvHistory;
	HistoryAdapter historyAdapter;
	TextView txtTitle;
	private ListView listHistory;

	private ArrayList<ItemTripHistory> listTripHistory;
	private int currentPage = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_list_history, container,
				false);
		lsvHistory = (PullToRefreshListView) view.findViewById(R.id.lsvHistory);
		listTripHistory = new ArrayList<ItemTripHistory>();

		initControl(view);
		initUI(view);
		initUIInThis(view);
		initMenuButton(view);
		setHeaderTitle(R.string.lbl_trip_history);
		return view;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {

		} else {
			currentPage = 1;
			listTripHistory.clear();
			getData();
		}
	}

	public void changeLanguage() {
		txtTitle.setText(R.string.lbl_trip_history);

	}

	private void initControl(View view) {
		txtTitle =  view.findViewById(R.id.lblTitle);
	}

	public void initUIInThis(View view) {
		historyAdapter = new HistoryAdapter(mainActivity, listTripHistory);
		lsvHistory.setAdapter(historyAdapter);
		lsvHistory.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				/*GlobalValue.getInstance().currentHistory = listTripHistory
						.get(position - 1);
				mainActivity.gotoActivity(DetailTripHistoryActivity.class);*/
			}

		});

		historyAdapter = new HistoryAdapter(mainActivity, listTripHistory);
		listHistory = lsvHistory.getRefreshableView();
		listHistory.setAdapter(historyAdapter);
		historyAdapter.notifyDataSetChanged();

		lsvHistory.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				currentPage = 1;
				listTripHistory.clear();
				getData();
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				getData();
			}
		});
	}

	// get data trip history
	private void getData() {
		ModelManager.showTripHistory(PreferencesManager.getInstance(self)
				.getToken(), currentPage + "", self, true,
				new ModelManagerListener() {
					@Override
					public void onSuccess(String json) {
						if (ParseJsonUtil.isSuccess(json)) {
							if (!ParseJsonUtil.parseTripHistory(json).isEmpty()) {
								listTripHistory.addAll(ParseJsonUtil
										.parseTripHistory(json));
								historyAdapter.setArrViews(listTripHistory);
								historyAdapter.notifyDataSetChanged();
								lsvHistory.onRefreshComplete();
								currentPage++;
							} else {
								lsvHistory.onRefreshComplete();
								showToast(getResources().getString(
										R.string.message_have_no_more_data));
							}
						} else {
							showToast(ParseJsonUtil.getMessage(json));
							lsvHistory.onRefreshComplete();
						}
					}

					@Override
					public void onError() {
						showToast(getResources().getString(
								R.string.message_have_some_error));
						lsvHistory.onRefreshComplete();
					}
				});
	}

}
